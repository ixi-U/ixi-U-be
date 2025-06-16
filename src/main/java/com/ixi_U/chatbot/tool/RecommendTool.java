package com.ixi_U.chatbot.tool;

import com.ixi_U.chatbot.extractor.FilterConditions;
import com.ixi_U.chatbot.extractor.MetadataExtractorImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendTool {

    private final Neo4jChatMemoryRepository chatMemoryRepository;
    private final Neo4jVectorStore neo4jVectorStore;
    private final MetadataExtractorImpl metadataExtractorImpl;

    @Tool(description = "요금제에 대한 추천 요청이 아닌 경우 '요금제를 물어봐주세요@@!!@@' 라고 응답하기")
    String defaultResponse() {

        log.info("defaultResponse Tool 동작");


        return "요금제를 물어봐주세요@@!!@@";
    }

    @Tool(description = "STK, KT 같은 U+ 통신사 이외에 대한 요금제 추천 요청이 들어올 경우 거부하기", returnDirect = true)
    String denyResponse() {

        log.info("denyResponse Tool 동작");


        return "타 통신사 요금제는 추천드릴 수 없습니다!";
    }

    @Tool(description = "욕설이나 부적절한 요청이 들어올 경우 거부하기", returnDirect = true)
    String badRequestResponse() {

        log.info("badRequestResponse Tool 동작");


        return "타 통신사 요금제는 추천드릴 수 없습니다!";
    }

    @Tool(description = "요금제에 대한 추천 요청이 들어올 경우 유사도 검색을 통해 요금제 추천하기")
    List<Document> recommendPlan(
            @ToolParam(description = "사용자의 자연어 요금제 추천 요청 쿼리") String userRequest,
            ToolContext toolContext) {

        log.info("recommendPlan Tool 동작");
        log.info("userRequest = {}", userRequest);
        log.info("userId = {}", toolContext.getContext().get("userId"));

        List<Message> byConversationId = chatMemoryRepository.findByConversationId((String) toolContext.getContext().get("userId"));


        return similaritySearchPlan(userRequest);
    }

    @Tool(description = "사용자 요청에서 혜택 관련된 정보가 있으면 정확히 어떤 혜택이 있는지 유사도 검색 기능 제공")
    void test(){
        log.info("동작 확인");
    }

    /**
     * 벡터 유사도 검색
     */
    private List<Document> similaritySearchPlan(String request){

        //--start 키워드 추출을 위한 필터식 생성 로직 (미완)
//        FilterConditions filterConditions = metadataExtractorImpl.extractFilters(request);
//        FilterExpressionBuilder b = new FilterExpressionBuilder();
//        Filter.Expression build = b.eq("test", "BG").build();

        List<Document> documents = neo4jVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request)
                        .similarityThreshold(0.1)
                        .topK(10)
                        .build());

        System.out.println("documents.size() = " + documents.size());
        
        for (Document document : documents) {
            System.out.println("document = " + document);
            System.out.println("document.getScore() = " + document.getScore());
            System.out.println("document.getText() = " + document.getText());

            Map<String, Object> metadata = document.getMetadata();

            System.out.println("document.getMetadata() = " + metadata);
            System.out.println("metadata.keySet() = " + metadata.keySet());
            System.out.println("metadata.values() = " + metadata.values());
            System.out.println("===========================================================================");
        }

        return documents;
    }
}
