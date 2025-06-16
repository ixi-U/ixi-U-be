package com.ixi_U.chatbot.advisor;

import com.ixi_U.chatbot.extractor.DataLimitCondition;
import com.ixi_U.chatbot.extractor.FilterConditions;
import com.ixi_U.chatbot.extractor.MonthlyPriceCondition;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomQuestionAnswerAdvisor implements StreamAdvisor {

    private final VectorStore vectorStore;
    private final int defaultTopK;

    public CustomQuestionAnswerAdvisor(VectorStore vectorStore) {
        this(vectorStore, 5);
    }

    public CustomQuestionAnswerAdvisor(VectorStore vectorStore, int defaultTopK) {
        this.vectorStore = vectorStore;
        this.defaultTopK = defaultTopK;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {

        return Mono.fromCallable(() -> performVectorSearch(chatClientRequest))
                .flatMapMany(streamAdvisorChain::nextStream)
                .doOnError(error -> handleVectorSearchError(chatClientRequest, error));
    }

    private ChatClientRequest performVectorSearch(ChatClientRequest chatClientRequest) {
        try {
            // 1. 이전 Advisor에서 추출한 필터 정보 가져오기
            FilterConditions filters = (FilterConditions) chatClientRequest.context()
                    .get("extractedFilters");
            String cleanQuery = (String) chatClientRequest.context()
                    .get("cleanQuery");
            String originalQuery = (String) chatClientRequest.context()
                    .get("originalQuery");

            // 2. 벡터 검색 수행
            List<Document> documents = executeVectorSearch(cleanQuery, filters);

            // 3. 검색 결과를 시스템 컨텍스트로 변환
            String systemContext = buildSystemContext(documents, filters);

            // 4. 로깅
            logVectorSearch(cleanQuery, filters, documents.size());

            // 5. 원본 사용자 쿼리로 복원하고 시스템 메시지 추가한 새로운 Prompt 생성
            Prompt modifiedPrompt = createPromptWithSystemContext(
                    chatClientRequest.prompt(), originalQuery, systemContext);

            // 6. 새로운 요청 생성
            return chatClientRequest.mutate()
                    .prompt(modifiedPrompt)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Vector search failed", e);
        }
    }

    private Prompt createPromptWithSystemContext(Prompt originalPrompt, String originalQuery, String systemContext) {
        // 원본 사용자 쿼리로 복원
        List<Message> restoredMessages = originalPrompt.getInstructions().stream()
                .map(message -> {
                    if (message instanceof UserMessage) {
                        return new UserMessage(originalQuery);
                    }
                    return message;
                })
                .toList();

        // 시스템 메시지 추가
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(new SystemMessage(systemContext));
        allMessages.addAll(restoredMessages);

        return new Prompt(allMessages, originalPrompt.getOptions());
    }

    private List<Document> executeVectorSearch(String cleanQuery, FilterConditions filters) {
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(cleanQuery)
                .topK(defaultTopK);

        // 필터 조건 적용
        if (filters != null) {
            Filter.Expression filterExpression = buildFilterExpression(filters);
            if (filterExpression != null) {
                searchBuilder.filterExpression(filterExpression);
            }
        }

        SearchRequest searchRequest = searchBuilder.build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private Filter.Expression buildFilterExpression(FilterConditions filters) {

        List<Filter.Expression> expressions = new ArrayList<>();

        // 가격 필터 (monthlyPrice)
        if (filters.getMonthlyPriceCondition() != null) {
            MonthlyPriceCondition monthlyPriceCondition = filters.getMonthlyPriceCondition();
            expressions.add(createPriceFilterExpression(monthlyPriceCondition));
        }

        // 데이터 용량 필터 (mobileDataLimitMb)
        if (filters.getMobileDataLimitMbCondition() != null) {
            DataLimitCondition dataCondition = filters.getMobileDataLimitMbCondition();
            expressions.add(createDataLimitFilterExpression(dataCondition));
        }

        // 번들 혜택 필터 (BundledBenefitNames)
        if (!filters.getBundledBenefitNames().isEmpty()) {
            expressions.add(new Filter.Expression(
                    Filter.ExpressionType.IN,
                    new Filter.Key("BundledBenefitNames"),
                    new Filter.Value(filters.getBundledBenefitNames())
            ));
        }

        // 단일 혜택 필터 (SingleBenefitNames)
        if (!filters.getSingleBenefitNames().isEmpty()) {
            expressions.add(new Filter.Expression(
                    Filter.ExpressionType.IN,
                    new Filter.Key("SingleBenefitNames"),
                    new Filter.Value(filters.getSingleBenefitNames())
            ));
        }

        // 혜택 유형 필터 (SingleBenefitTypes)
        if (!filters.getSingleBenefitTypes().isEmpty()) {
            expressions.add(new Filter.Expression(
                    Filter.ExpressionType.IN,
                    new Filter.Key("SingleBenefitTypes"),
                    new Filter.Value(filters.getSingleBenefitTypes())
            ));
        }

        // 요금제 이름 필터 (name)
        if (filters.getPlanName() != null && !filters.getPlanName().trim().isEmpty()) {
            expressions.add(new Filter.Expression(
                    Filter.ExpressionType.EQ,
                    new Filter.Key("name"),
                    new Filter.Value(filters.getPlanName())
            ));
        }

        // 여러 조건을 AND로 결합

        Filter.Expression expression = null;

        if (expressions.isEmpty()) {

            return expression;
        } else if (expressions.size() == 1) {

            expression = expressions.get(0);
        } else {

            // 여러 조건을 AND로 연결
            Filter.Expression result = expressions.get(0);

            for (int i = 1; i < expressions.size(); i++) {

                result = new Filter.Expression(Filter.ExpressionType.AND, result, expressions.get(i));
            }

            expression = result;
        }

        System.out.println("expression = " + expression);
        System.out.println("expression.toString() = " + expression.toString());

        return expression;
    }

    private Filter.Expression createPriceFilterExpression(MonthlyPriceCondition condition) {
        Filter.Key priceKey = new Filter.Key("monthlyPrice");
        Filter.Value priceValue = new Filter.Value(condition.getAmount());

        switch (condition.getOperator()) {
            case LESS_THAN_OR_EQUAL:
                return new Filter.Expression(Filter.ExpressionType.LTE, priceKey, priceValue);
            case GREATER_THAN_OR_EQUAL:
                return new Filter.Expression(Filter.ExpressionType.GTE, priceKey, priceValue);
            case LESS_THAN:
                return new Filter.Expression(Filter.ExpressionType.LT, priceKey, priceValue);
            case GREATER_THAN:
                return new Filter.Expression(Filter.ExpressionType.GT, priceKey, priceValue);
            case EQUAL:
                return new Filter.Expression(Filter.ExpressionType.EQ, priceKey, priceValue);
            default:
                throw new IllegalArgumentException("Unknown price operator: " + condition.getOperator());
        }
    }

    private Filter.Expression createDataLimitFilterExpression(DataLimitCondition condition) {
        Filter.Key dataKey = new Filter.Key("mobileDataLimitMb");
        Filter.Value dataValue = new Filter.Value(condition.getLimitMb());

        return switch (condition.getOperator()) {
            case LESS_THAN_OR_EQUAL -> new Filter.Expression(Filter.ExpressionType.LTE, dataKey, dataValue);
            case GREATER_THAN_OR_EQUAL -> new Filter.Expression(Filter.ExpressionType.GTE, dataKey, dataValue);
            case LESS_THAN -> new Filter.Expression(Filter.ExpressionType.LT, dataKey, dataValue);
            case GREATER_THAN -> new Filter.Expression(Filter.ExpressionType.GT, dataKey, dataValue);
            case EQUAL -> new Filter.Expression(Filter.ExpressionType.EQ, dataKey, dataValue);
            default -> throw new IllegalArgumentException("Unknown data limit operator: " + condition.getOperator());
        };
    }

    private String buildSystemContext(List<Document> documents, FilterConditions filters) {
        StringBuilder contextBuilder = new StringBuilder();

        contextBuilder.append("당신은 통신사 요금제 추천 전문가입니다.\n");
        contextBuilder.append("다음 검색 결과를 바탕으로 사용자의 요청에 맞는 요금제를 추천해주세요.\n\n");

        // 필터 조건 정보 추가
        if (filters != null) {
            contextBuilder.append("=== 사용자 필터 조건 ===\n");

            if (filters.getMonthlyPriceCondition() != null) {
                contextBuilder.append("- 가격 조건: ")
                        .append(formatPriceCondition(filters.getMonthlyPriceCondition()))
                        .append("\n");
            }

            if (filters.getMobileDataLimitMbCondition() != null) {
                contextBuilder.append("- 데이터 용량 조건: ")
                        .append(formatDataLimitCondition(filters.getMobileDataLimitMbCondition()))
                        .append("\n");
            }

            if (!filters.getBundledBenefitNames().isEmpty()) {
                contextBuilder.append("- 원하는 번들 혜택: ")
                        .append(String.join(", ", filters.getBundledBenefitNames()))
                        .append("\n");
            }

            if (!filters.getSingleBenefitNames().isEmpty()) {
                contextBuilder.append("- 원하는 개별 혜택: ")
                        .append(String.join(", ", filters.getSingleBenefitNames()))
                        .append("\n");
            }

            if (!filters.getSingleBenefitTypes().isEmpty()) {
                contextBuilder.append("- 원하는 혜택 유형: ")
                        .append(String.join(", ", filters.getSingleBenefitTypes()))
                        .append("\n");
            }

            if (filters.getPlanName() != null && !filters.getPlanName().trim().isEmpty()) {
                contextBuilder.append("- 요금제 이름: ")
                        .append(filters.getPlanName())
                        .append("\n");
            }

            contextBuilder.append("\n");
        }

        // 검색된 요금제 정보 추가
        contextBuilder.append("=== 검색된 요금제 정보 ===\n");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            contextBuilder.append(String.format("[요금제 %d]\n", i + 1));

            // 메타데이터에서 주요 정보 추출하여 구조화된 형태로 표시
            Map<String, Object> metadata = doc.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                contextBuilder.append("📋 기본 정보:\n");

                // 요금제 이름
                if (metadata.get("name") != null) {
                    contextBuilder.append("  - 요금제명: ").append(metadata.get("name")).append("\n");
                }

                // 월 요금
                if (metadata.get("monthlyPrice") != null) {
                    contextBuilder.append("  - 월 요금: ")
                            .append(String.format("%,d원", ((Number) metadata.get("monthlyPrice")).intValue()))
                            .append("\n");
                }

                // 데이터 제공량
                if (metadata.get("mobileDataLimitMb") != null) {
                    int dataMb = ((Number) metadata.get("mobileDataLimitMb")).intValue();
                    contextBuilder.append("  - 데이터 제공량: ");
                    if (dataMb >= 1024) {
                        contextBuilder.append(String.format("%.1fGB", dataMb / 1024.0));
                    } else {
                        contextBuilder.append(dataMb).append("MB");
                    }
                    contextBuilder.append("\n");
                }

                // 번들 혜택
                List<?> bundledBenefits = (List<?>) metadata.get("BundledBenefitNames");
                if (bundledBenefits != null && !bundledBenefits.isEmpty()) {
                    contextBuilder.append("  - 번들 혜택: ")
                            .append(bundledBenefits.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", ")))
                            .append("\n");
                }

                // 개별 혜택 유형
                List<?> benefitTypes = (List<?>) metadata.get("SingleBenefitTypes");
                if (benefitTypes != null && !benefitTypes.isEmpty()) {
                    contextBuilder.append("  - 혜택 유형: ")
                            .append(benefitTypes.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", ")))
                            .append("\n");
                }

                // 개별 혜택 (주요 몇 개만 표시)
                List<?> singleBenefits = (List<?>) metadata.get("SingleBenefitNames");
                if (singleBenefits != null && !singleBenefits.isEmpty()) {
                    contextBuilder.append("  - 주요 개별 혜택: ");
                    List<String> limitedBenefits = singleBenefits.stream()
                            .map(Object::toString)
                            .limit(5)  // 처음 5개만 표시
                            .collect(Collectors.toList());
                    contextBuilder.append(String.join(", ", limitedBenefits));
                    if (singleBenefits.size() > 5) {
                        contextBuilder.append(" 외 ").append(singleBenefits.size() - 5).append("개");
                    }
                    contextBuilder.append("\n");
                }

                contextBuilder.append("\n");
            }

            // 요금제 설명
            contextBuilder.append("📝 상세 설명:\n");
            contextBuilder.append(doc.getText()).append("\n");
            contextBuilder.append("─────────────────────────────────────\n\n");
        }

        contextBuilder.append("위 정보를 바탕으로 사용자의 요구사항에 가장 적합한 요금제를 추천하고, ");
        contextBuilder.append("각 요금제의 장단점과 사용자에게 특히 유용한 혜택들을 설명해주세요.\n");

        return contextBuilder.toString();
    }

    private String formatPriceCondition(MonthlyPriceCondition condition) {
        String amount = String.format("%,d원", condition.getAmount());
        return switch (condition.getOperator()) {
            case LESS_THAN_OR_EQUAL -> amount + " 이하";
            case LESS_THAN -> amount + " 미만";
            case GREATER_THAN_OR_EQUAL -> amount + " 이상";
            case GREATER_THAN -> amount + " 초과";
            case EQUAL -> amount;
        };
    }

    private String formatDataLimitCondition(DataLimitCondition condition) {
        int limitMb = condition.getLimitMb();
        String amount;
        if (limitMb >= 1024) {
            amount = String.format("%.1fGB", limitMb / 1024.0);
        } else {
            amount = limitMb + "MB";
        }

        switch (condition.getOperator()) {
            case LESS_THAN_OR_EQUAL:
                return amount + " 이하";
            case LESS_THAN:
                return amount + " 미만";
            case GREATER_THAN_OR_EQUAL:
                return amount + " 이상";
            case GREATER_THAN:
                return amount + " 초과";
            case EQUAL:
                return amount;
            default:
                return amount;
        }
    }

    private void logVectorSearch(String cleanQuery, FilterConditions filters, int resultCount) {
        System.out.println("=== Vector Search ===");
        System.out.println("Clean Query: " + cleanQuery);
        System.out.println("Applied Filters: " + filters);
        System.out.println("Results Count: " + resultCount);
        System.out.println("====================");
    }

    private void handleVectorSearchError(ChatClientRequest request, Throwable error) {
        String userQuery = extractUserQuery(request);
        System.err.println("Vector search failed for request: " + userQuery);
        System.err.println("Error: " + error.getMessage());
    }

    private String extractUserQuery(ChatClientRequest request) {
        return request.prompt().getInstructions().stream()
                .filter(message -> message instanceof UserMessage)
                .map(message -> ((UserMessage) message).getText())
                .findFirst()
                .orElse("");
    }

    @Override
    public String getName() {
        return "CustomQuestionAnswerAdvisor";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}