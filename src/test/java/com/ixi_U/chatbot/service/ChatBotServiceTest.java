package com.ixi_U.chatbot.service;

import static com.ixi_U.util.constants.TestConstants.CHATBOT_WELCOME_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.testutil.TestDataFactory;
import com.ixi_U.common.exception.GeneralException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChatBotServiceTest {

    @Mock
    ChatClient descriptionClient;

    Validator validator;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    ChatBotService chatBotService;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("웰컴 메세지를 응답한다")
    public void getWelcomeMessage() {

        //given

        //when
        Flux<String> welcomeMessage = chatBotService.getWelcomeMessage();

        //then
        StepVerifier.create(welcomeMessage.collectList())
                .expectNextMatches(chars -> String.join("", chars).equals(CHATBOT_WELCOME_MESSAGE))
                .verifyComplete();
    }

    @Nested
    class WhenGeneratePlanDescriptionRequested {

        @Test
        @DisplayName("LLM 응답이 null일 경우 예외를 발생한다")
        public void getPlanDescription() throws Exception {

            //given
            GeneratePlanDescriptionRequest plan = TestDataFactory.createValidPlan();

            //Mocking
            ChatClient.ChatClientRequestSpec requestSpec = mock(
                    ChatClient.ChatClientRequestSpec.class);
            ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

            given(objectMapper.writeValueAsString(any())).willReturn("{\"id\":\"test\"}");
            given(descriptionClient.prompt()).willReturn(requestSpec);
            given(requestSpec.user(anyString())).willReturn(requestSpec);
            given(requestSpec.call()).willReturn(callResponseSpec);
            given(callResponseSpec.content()).willReturn(null);

            //when & then
            assertThrows(GeneralException.class,
                    () -> chatBotService.getPlanDescription(plan));
        }

        @Test
        @DisplayName("유효한 요청이면 통과된다")
        public void validPlanRequest() {

            //given
            GeneratePlanDescriptionRequest plan = TestDataFactory.createValidPlan();

            //when
            Set<ConstraintViolation<GeneratePlanDescriptionRequest>> validate = validator.validate(plan);

            //then
            assertTrue(validate.isEmpty());
        }
    }
}
