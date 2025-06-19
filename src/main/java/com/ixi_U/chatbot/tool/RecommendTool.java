package com.ixi_U.chatbot.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
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
    private final Neo4jChatMemoryRepository neo4jChatMemoryRepository;

    @Tool(description = """
            사용자가 특정 조건(가격, 데이터량, 혜택 등)에 맞는 요금제를 찾거나 추천받고 싶을 때 사용한다.
            예: '3만원대 요금제 추천해줘', '무제한 데이터 요금제 찾아줘', '넷플릭스 혜택 있는 요금제는?
            '학생용 저렴한 요금제 추천' 등의 조건부 검색이나 추천 요청에서만 동작한다.
            """)
    List<Document> recommendPlan(
            @ToolParam(description = "사용자의 특정 조건이 포함된 요금제 추천/검색 쿼리") String userQuery,
            final ToolContext toolContext) {

        log.info("요금제 추천 툴 동작");

        String filterExpression = (String) toolContext.getContext().get(ToolContextKey.FILTER_EXPRESSION.getKey());
        String userId = (String) toolContext.getContext().get(ToolContextKey.USER_ID.getKey());

        List<Document> documents = performVectorSearch(userQuery, filterExpression);

        log.info("userRequest = {}", userQuery);
        log.info("{} = {}", ToolContextKey.FILTER_EXPRESSION, filterExpression);
        log.info("{} = {}", ToolContextKey.USER_ID, userId);

        return documents;
    }

    @Tool(description = """
            이전에 추천받은 요금제를 제외하고 새로운 요금제를 추천합니다.
            "다른 요금제", "이거 말고", "또 다른 추천" 등의 요청에 사용됩니다.
            새로운 필터 표현식이 존재하지 않을 경우 이전 표현식을 그대로 사용합니다.
            """)
    List<Document> recommendExcludePreviousPlan(
            @ToolParam(description = "사용자의 특정 조건이 포함된 요금제 추천/검색 쿼리") String userQuery,
            @ToolParam(description = "가장 최근 대화에서 사용한 필터 표현식을 가져온다") String filterExpression,
            final ToolContext toolContext
    ) {

        log.info("이전 요금제 제외 추천 툴 동작");
        String userId = (String) toolContext.getContext().get(ToolContextKey.USER_ID.getKey());
        log.info("userRequest = {}", userQuery);
        log.info("{} = {}", ToolContextKey.FILTER_EXPRESSION, filterExpression);
        log.info("{} = {}", ToolContextKey.USER_ID, userId);

        List<Document> documents = null;

        return documents;
    }

    /**
     * 필터 표현식을 사용하지 않는 유사도 검색
     */
    private List<Document> similaritySearchPlanWithoutFilterExpression(String userQuery) {

        log.info("필터 표현식을 사용하지 않는 유사도 검색");

        List<Document> documents = planVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .similarityThreshold(0.6)
                        .topK(3)
                        .build());

        log.info("조회된 건 수 = {}", documents.size());

        return documents;
    }

    /**
     * 필터 표현식을 이용한 유사도 검색
     */
    private List<Document> similaritySearchPlan(String userQuery, String filterExpression) {

        log.info("필터 표현식을 이용한 유사도 검색");

        List<Document> documents = planVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .similarityThreshold(0.1)
                        .topK(3)
                        .filterExpression(filterExpression)
                        .build());

        log.info("조회된 건 수 = {}", documents.size());

        return documents;
    }

    /**
     * 벡터 검색 동작 분기
     */
    private List<Document> performVectorSearch(String userQuery, String filterExpression) {

        log.info("벡터 검색 수행: userQuery = {}, filterExpression = {}", userQuery, filterExpression);

        if (filterExpression == null || filterExpression.equals("ALL_DATA")) {

            return similaritySearchPlanWithoutFilterExpression(userQuery);
        }

        return similaritySearchPlan(userQuery, filterExpression);
    }
}
