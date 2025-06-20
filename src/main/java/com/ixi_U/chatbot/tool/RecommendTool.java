package com.ixi_U.chatbot.tool;

import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
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

        log.info("---요금제 추천 툴 동작");

        try {
            String filterExpression = (String) toolContext.getContext().get(ToolContextKey.FILTER_EXPRESSION.getKey());
            String userId = (String) toolContext.getContext().get(ToolContextKey.USER_ID.getKey());

            log.info("userRequest = {}", userQuery);
            log.info("{} = {}", ToolContextKey.FILTER_EXPRESSION, filterExpression);
            log.info("{} = {}", ToolContextKey.USER_ID, userId);

            return performVectorSearch(userQuery, filterExpression);

        } catch (Exception e) {

            throw new GeneralException(ChatBotException.RECOMMEND_PLAN_TOOL_ERROR);
        }
    }

    @Tool(description = """
            이전 대화의 요금제에 대한 내용이 나올경우 사용한다
            
            - "그 중에서 ~만", "그것들 중에서", "앞에서 추천한 것 중에서"
            - "이전 추천에서 ~조건", "방금 추천한 것에서"
            
            와 같은 맥락의 요청이 들어올 경우 사용한다
            
            이 도구는 대화 컨텍스트의 이전 추천 결과를 기반으로:
            1. 사용자의 추가 조건을 파싱
            2. 기존 필터와 새 조건을 결합
            3. 조건에 맞는 요금제만 필터링하여 반환
            """)
    List<Message> recommendUsingChatMemory(final ToolContext toolContext) {

        log.info("---대화 내역 기반 추천 툴 동작");

        try {
            String userId = (String) toolContext.getContext().get(ToolContextKey.USER_ID.getKey());

            log.info("{} = {}", ToolContextKey.USER_ID, userId);

            return neo4jChatMemoryRepository.findByConversationId(userId);
        } catch (Exception e) {

            log.error(e.getMessage());

            throw new GeneralException(ChatBotException.RECOMMEND_USING_CHAT_MEMORY_TOOL_ERROR);
        }
    }

    /**
     * 필터 표현식을 사용하지 않는 유사도 검색
     */
    private List<Document> similaritySearchPlanWithoutFilterExpression(String userQuery) {

        log.info("필터 표현식을 사용하지 않는 유사도 검색");

        List<Document> documents = planVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .similarityThreshold(0.5)
                        .topK(10)
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
                        .similarityThreshold(0.5)
                        .topK(10)
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
