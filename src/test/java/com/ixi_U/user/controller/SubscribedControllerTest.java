package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.common.config.FakeNeo4jAuditingConfig;
import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.dto.response.ShowSubscribedHistoryResponse;
import com.ixi_U.user.service.SubscribedService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(FakeNeo4jAuditingConfig.class)
class SubscribedControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SubscribedService subscribedService;

    @Nested
    class SubscribedController {

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
                    .updateSubscribed(eq(userId), eq(request));
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

        @Test
        @DisplayName("구독 히스토리 정상 반환")
        void getSubscribedHistoryByUserId_success() throws Exception {
            // given
            List<ShowSubscribedHistoryResponse> responses = List.of(
                    new ShowSubscribedHistoryResponse(1L, "베이직",
                            LocalDateTime.of(2024, 6, 1, 10, 0)),
                    new ShowSubscribedHistoryResponse(2L, "프리미엄",
                            LocalDateTime.of(2024, 6, 2, 11, 0)),
                    new ShowSubscribedHistoryResponse(3L, "스페셜",
                            LocalDateTime.of(2024, 6, 3, 12, 0))
            );
            when(subscribedService.findSubscribedHistoryByUserId("user-1")).thenReturn(responses);

            // when & then
            mockMvc.perform(get("/subscribed/{userId}/history", "user-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].planName").value("베이직"))
                    .andExpect(jsonPath("$[1].planName").value("프리미엄"))
                    .andExpect(jsonPath("$[2].planName").value("스페셜"));
        }

        @AfterEach
        void resetMocks() {
            Mockito.reset(subscribedService);
        }

    }
}
