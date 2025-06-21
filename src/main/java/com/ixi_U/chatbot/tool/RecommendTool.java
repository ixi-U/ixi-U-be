package com.ixi_U.chatbot.tool;

import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.chatbot.tool.dto.MostDataAmountPlanToolDto;
import com.ixi_U.chatbot.tool.dto.MostReviewPointPlanToolDto;
import com.ixi_U.chatbot.tool.dto.MostReviewedPlanToolDto;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
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

    private final PlanRepository planRepository;

    @Tool(description = """
            사용자가 특정 조건(가격, 데이터량, 혜택 등)에 맞는 요금제를 찾거나 추천받고 싶을 때 사용한다.
            또한 특정 그룹, 컨셉, 브랜드와 관련된 요금제 추천 요청에도 사용된다.
            예시:
            - 조건부 검색: '3만원대 요금제 추천해줘', '무제한 데이터 요금제 찾아줘', '넷플릭스 혜택 있는 요금제는?'
            - 사용자 그룹: '학생용 저렴한 요금제 추천', '시니어 요금제', '군인 요금제'
            - 컨셉/브랜드: '아이돌 요금제', 'BTS 요금제', '게이머 요금제', '유튜버 요금제'
            - 라이프스타일: '미니멀리스트 요금제', '프리미엄 요금제', '가성비 요금제'
            - 기타: '특별 프로모션 요금제', '신규 출시 요금제', '인기 있는 요금제'
            등 요금제와 관련된 모든 추천/검색 요청에서 동작한다.
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

            log.error(e.getMessage());

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

    @Tool(description = """
            가장 많은 리뷰 점수를 받은 요금제 추천
            가장 높은 별점을 받은 요금제 추천
            """)
    MostReviewPointPlanToolDto findMostReviewPoint(
            @ToolParam(description = "N 번째로 높은 리뷰 점수를 받은 요금제를 추천할 때 N을 숫자로 반환한다.") int skip) {

        log.info("---리뷰 점수가 높은 요금제 추천 툴 동작");
        log.info("N 번째 = {}", skip);
        try {
            MostReviewPointPlanToolDto mostReviewPointPlan = planRepository.findMostReviewPointPlan(skip);

            log.info("리뷰 점수 : {}", mostReviewPointPlan.reviewPointAverage());

            return mostReviewPointPlan;
        } catch (Exception e) {

            log.error(e.getMessage());

            return null;
        }
    }

    @Tool(description = """
            가장 많은 데이터를 가진 요금제를 조회할 때 사용
            """)
    MostDataAmountPlanToolDto findMostDataAmountPlan(
            @ToolParam(description = "N 번째로 많은 데이터를 가진 요금제를 추천할 때 N을 숫자로 반환한다.") int skip){

        log.info("---가장 많은 데이터를 가진 요금제 추천 툴 동작");
        log.info("N 번째 = {}", skip);

        try {
            MostDataAmountPlanToolDto mostDataAmountPlan = planRepository.findByMostDataAmount(skip);

            log.info("요금제 이름 : {}", mostDataAmountPlan.name());
            log.info("데이터 제공량 : {}", mostDataAmountPlan.mobileDataLimitMb());

            return mostDataAmountPlan;
        } catch (Exception e) {

            log.error(e.getMessage());

            return null;
        }
    }

    @Tool(description = """
            이 도구는 다음과 같은 맥락에서만 사용됩니다:
            
            1. 필터 표현식이 없고 특별한 정렬 조건도 없을 때
            2. "아무 요금제나 추천해줘" 같은 일반적인 추천 요청
            3. "대중적인 요금제", "인기 요금제", "많이 사용하는 요금제" 요청
            4. "리뷰 많은 요금제", "평점 좋은 요금제" 요청
            
            다음과 같은 경우에는 사용하지 않습니다:
            - "가장 비싼/저렴한 요금제" (가격 정렬 조건)
            - "가장 많은 데이터" (데이터 정렬 조건)
            - "가장 X한" 형태의 최상급 표현
            - 특정 조건이나 필터가 있는 경우
            """)
    MostReviewedPlanToolDto findMostReviewedPlan(
            @ToolParam(description = "N 번째로 많은 리뷰를 받은 요금제를 추천할 때 N을 숫자로 반환한다.") int skip) {

        log.info("---리뷰가 많은 요금제 추천 툴 동작");
        log.info("N 번째 = {}", skip);
        try {
            MostReviewedPlanToolDto mostReviewedPlan = planRepository.findMostReviewedPlan(skip);

            log.info("리뷰 수 : {}", mostReviewedPlan.reviewedCount());

            return mostReviewedPlan;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
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
                        .topK(100)
                        .build());

        log.info("조회된 건 수 = {}", documents.size());

        for (Document document : documents) {
            log.info("이름 : {}, 유사도 : {}", document.getMetadata().get("name"), document.getScore());
        }

        return documents;
    }

    /**
     * 필터 표현식을 이용한 유사도 검색
     */
    private List<Document> similaritySearchPlan(String userQuery, String filterExpression) {

        log.info("필터 표현식을 이용한 유사도 검색");

        log.info("filterExpression = {}", filterExpression);

        log.info("실제 무제한 데이터 수 : {}",planRepository.findByMobileDataLimitMb(2147483647).size());

        List<Document> documents = planVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .similarityThreshold(0.5)
                        .filterExpression(filterExpression)
                        .topK(100)
                        .build());

        log.info("조회된 건 수 = {}", documents.size());

        for (Document document : documents) {
            log.info("이름 : {}, 유사도 : {}", document.getMetadata().get("name"), document.getScore());
        }

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
