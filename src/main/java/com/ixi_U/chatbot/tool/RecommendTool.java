package com.ixi_U.chatbot.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendTool {

    @Qualifier("planVectorStore")
    private final VectorStore planVectorStore;

    @Tool(description = """
            사용자가 특정 조건(가격, 데이터량, 혜택 등)에 맞는 요금제를 찾거나 추천받고 싶을 때 사용한다.
            예: '3만원대 요금제 추천해줘', '무제한 데이터 요금제 찾아줘', '넷플릭스 혜택 있는 요금제는?
            '학생용 저렴한 요금제 추천' 등의 조건부 검색이나 추천 요청에서만 동작한다.
            """)
    List<Document> recommendPlan(
            @ToolParam(description = "사용자의 특정 조건이 포함된 요금제 추천/검색 쿼리") String userQuery,
            final ToolContext toolContext) {

        log.info("recommendPlan Tool 동작");

        String filterExpression = (String) toolContext.getContext().get(ToolContextKey.FILTER_EXPRESSION.getKey());
        String userId = (String) toolContext.getContext().get(ToolContextKey.USER_ID.getKey());

        log.info("userRequest = {}", userQuery);
        log.info("{} = {}", ToolContextKey.FILTER_EXPRESSION, filterExpression);
        log.info("{} = {}", ToolContextKey.USER_ID, userId);

        if (filterExpression.equals("ALL_DATA")){

//            return similaritySearchPlan(userQuery);
        }

        return similaritySearchPlan(userQuery, filterExpression);
    }

    /**
     * 벡터 유사도 검색
     */
    private List<Document> similaritySearchPlan(String userQuery, String filterExpression) {

        List<Document> documents = null;

        log.info("유사도 검사 메서드 동작 시작");

        try {
            documents = planVectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(userQuery)
                            .similarityThreshold(0.1)
                            .topK(50)
                            .filterExpression(filterExpression)
                            .build());

            log.info("조회된 건 수 = {}", documents.size());
            log.info("조회 디테일 = {}", documents);
            log.info("=======================================");

        } catch (Exception e) {

            log.error("예외 발생 ", e);
        }

        return documents;
    }
}
