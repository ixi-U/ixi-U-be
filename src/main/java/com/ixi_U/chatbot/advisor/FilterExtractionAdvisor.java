package com.ixi_U.chatbot.advisor;

import com.ixi_U.chatbot.extractor.FilterConditions;
import com.ixi_U.chatbot.extractor.MetadataExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FilterExtractionAdvisor implements StreamAdvisor {

    private final MetadataExtractor metadataExtractor;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {

        return Mono.fromCallable(() -> {

            // 1. 사용자 쿼리에서 필터 조건 추출
            String userQuery = extractUserQuery(chatClientRequest);

            FilterConditions filters = metadataExtractor.extractFilters(userQuery);

            // 2. 벡터 검색용 정제된 쿼리 생성
            String cleanQuery = metadataExtractor.generateCleanQuery(userQuery, filters);

            // 3. 추출된 정보를 context에 저장
            ChatClientRequest modifiedRequest = chatClientRequest.mutate()
                    .context("extractedFilters", filters)
                    .context("cleanQuery", cleanQuery)
                    .context("originalQuery", userQuery)
                    .prompt(createModifiedPrompt(chatClientRequest.prompt(), cleanQuery))
                    .build();

            // 4. 로깅
            logFilterExtraction(userQuery, filters, cleanQuery);

            return modifiedRequest;
        }).flatMapMany(streamAdvisorChain::nextStream)
        .doOnError(error -> handleFilterExtractionError(chatClientRequest, error));
    }

    @Override
    public String getName() {
        return "FilterExtractionAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private Prompt createModifiedPrompt(Prompt originalPrompt, String cleanQuery) {

        List<Message> modifiedMessage = originalPrompt.getInstructions().stream()
                .map(message -> {

                    if (message instanceof UserMessage) {

                        return new UserMessage(cleanQuery);
                    }

                    return message;
                })
                .toList();

        return new Prompt(modifiedMessage, originalPrompt.getOptions());
    }

    private String extractUserQuery(ChatClientRequest request) {

        return request.prompt().getInstructions().stream()
                .filter(message -> message instanceof UserMessage)
                .map(message -> ((UserMessage) message).getText())
                .reduce((first,second)->second)
                .orElse("");
    }

    private void handleFilterExtractionError(ChatClientRequest request, Throwable error) {

        String query = extractUserQuery(request);

        log.error("Filter extraction failed : {}", query);
        log.error("Error : {}", error.getMessage());
    }

    private void logFilterExtraction(String originalQuery, FilterConditions filters, String cleanQuery) {

//        log.info("=== Filter Extraction ===");
//        log.info("Original Query: {}", originalQuery);
//        log.info("filters.getPlanName(): {}", filters.getPlanName());
//        log.info("filters.getPriceCondition(): {}", filters.getPriceCondition());
//        log.info("filters.getBundledBenefits(): {}", filters.getBundledBenefits());
//        log.info("filters.getSingleBenefits(): {}", filters.getSingleBenefits());
//        log.info("filters.getDataLimitCondition(): {}",filters.getDataLimitCondition());
//        log.info("filters.getBenefitTypes(): {}", filters.getBenefitTypes());
//        log.info("Clean Query: {}", cleanQuery);
//        log.info("========================");

//        System.out.println("=== Filter Extraction ===");
//        System.out.println("Original Query: " + originalQuery);
//        System.out.println("getPlanName: " + filters.getPlanName());
//        System.out.println("getPriceCondition: " + filters.getPriceCondition());
//        System.out.println("getBundledBenefits: " + filters.getBundledBenefits());
//        System.out.println("getSingleBenefits: " + filters.getSingleBenefits());
//        System.out.println("getDataLimitCondition: " + filters.getDataLimitCondition());
//        System.out.println("getBenefitTypes: " + filters.getBenefitTypes());
//        System.out.println("Clean Query: " + cleanQuery);
//        System.out.println("========================");
    }
}
