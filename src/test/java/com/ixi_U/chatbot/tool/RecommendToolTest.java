package com.ixi_U.chatbot.tool;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.repository.PlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RecommendToolTest {

    @Mock
    private VectorStore planVectorStore;

    @Mock
    private Neo4jChatMemoryRepository neo4jChatMemoryRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private RecommendTool recommendTool;

    @Nested
    class WhenRecommendToolActive {

        @Nested
        class WhenSuccess {

            @Test
            @DisplayName("LLM 이 호출한 도구가 정상 응답한다.")
            public void toolSuccessResponse() {

                //given
                String userQuery = "만원 이산 요금제 추천해줘";

                ToolContext toolContext = new ToolContext(Map.of(
                        ToolContextKey.USER_ID.getKey(), "testUser",
                        ToolContextKey.FILTER_EXPRESSION.getKey(), "monthlyPrice > 10000"
                ));

                List<Document> expectedDocuments = List.of(
                        new Document("plan1", Map.of("name", "프리미엄 요금제", "monthlyPrice", 19000)),
                        new Document("plan2", Map.of("name", "일반 요금제", "monthlyPrice", 11000))
                );

                given(planVectorStore.similaritySearch(any(SearchRequest.class))).willReturn(expectedDocuments);

                //when
                List<Document> documents = recommendTool.recommendPlan(userQuery, toolContext);

                //then
                assertThat(documents)
                        .isNotNull()
                        .hasSize(2)
                        .extracting(doc -> doc.getMetadata().get("name"))
                        .containsExactly("프리미엄 요금제", "일반 요금제");

                assertThat(documents)
                        .extracting(doc -> doc.getMetadata().get("monthlyPrice"))
                        .allMatch(price -> (Integer) price > 10000);
            }
        }

        @Nested
        class WhenFail {

            @Test
            @DisplayName("userQuery가 null일 경우 예외 처리")
            public void tool_fail_when_user_query_null() throws Exception {

                //given
                String userQuery = null;
                ToolContext toolContext = new ToolContext(Map.of(
                        ToolContextKey.USER_ID.getKey(), "testUser",
                        ToolContextKey.FILTER_EXPRESSION.getKey(), "monthlyPrice > 10000"
                ));

                //when & then
                assertThatThrownBy(()->recommendTool.recommendPlan(userQuery,toolContext))
                        .isInstanceOf(GeneralException.class)
                        .hasMessage("요금제 추천 도구에서 에러가 발생했습니다.");
            }
        }
    }
}
