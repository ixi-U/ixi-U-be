package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.user.dto.CreateSubscribedRequest;
import com.ixi_U.user.service.SubscribedService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = SubscribedController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class SubscribedControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    SubscribedService subscribedService;

    @Test
    @DisplayName("요금제 등록 후 엔드포인트 정상 호출 시 200 OK를 반환한다")
    void updateSubscribedSuccess() throws Exception {
        String userId = "test-user";
        CreateSubscribedRequest request = new CreateSubscribedRequest("plan-1");

        mockMvc.perform(post("/subscribed/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(subscribedService)
                .updateSubscribed(eq(userId), any(CreateSubscribedRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 요청 데이터로 호출 시 400 Bad Request를 반환한다")
    void updateSubscribedWithInvalidRequest() throws Exception {
        String userId = "test-user";
        CreateSubscribedRequest invalidRequest = new CreateSubscribedRequest(""); // 빈 planId

        mockMvc.perform(post("/subscribed/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(subscribedService);
    }

    @TestConfiguration
    static class DisableNeo4jAuditingConfig {

        @Bean("neo4jAuditingHandler")
        public Object fakeAuditingHandler() {
            return new Object();
        }
    }
}
