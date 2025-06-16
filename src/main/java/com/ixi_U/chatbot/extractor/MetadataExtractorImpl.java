package com.ixi_U.chatbot.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MetadataExtractorImpl implements MetadataExtractor {

    // 가격 관련 패턴
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(\\d+)만원?\\s*(이하|이상|미만|초과|이내|넘|아래|위)|" +
                    "(\\d+)원?\\s*(이하|이상|미만|초과|이내|넘|아래|위)|" +
                    "(이하|이상|미만|초과|이내|넘|아래|위)\\s*(\\d+)만원?|" +
                    "(이하|이상|미만|초과|이내|넘|아래|위)\\s*(\\d+)원?"
    );

    // 데이터 용량 관련 패턴
    private static final Pattern DATA_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(GB|기가|테라|TB|MB|메가)\\s*(이하|이상|미만|초과|이내|넘|아래|위)|" +
                    "(이하|이상|미만|초과|이내|넘|아래|위)\\s*(\\d+(?:\\.\\d+)?)\\s*(GB|기가|테라|TB|MB|메가)|" +
                    "무제한\\s*데이터|데이터\\s*무제한"
    );

    // 요금제 이름 패턴
    private static final Pattern PLAN_NAME_PATTERN = Pattern.compile(
            "(\\w+)\\s*(요금제|플랜)|" +
                    "요금제\\s*(\\w+)|" +
                    "플랜\\s*(\\w+)"
    );

    // 번들 혜택 키워드 매핑
    private static final List<String> BUNDLED_BENEFIT_KEYWORDS = List.of(
            "디즈니+ 티빙", "삼성팩", "애플디바이스팩", "멀티팩"
    );

    // 개별 혜택 키워드 매핑
    private static final List<String> SINGLE_BENEFIT_KEYWORDS = List.of(
            "음악", "지니뮤직", "바이브",
            "워치", "버즈", "에어팟", "아이패드", "갤럭시",
            "로밍", "멤버십", "VIP", "할인", "프로모션",
            "아이들나라", "밀리의서재", "안심서비스", "피싱", "해킹"
    );

    // 혜택 유형 키워드 매핑
    private static final List<String> BENEFIT_TYPE_KEYWORDS = List.of(
            "할인", "구독", "스마트기기", "엔터테인먼트", "음악", "보안", "멤버십"
    );

    @Override
    public FilterConditions extractFilters(String query) {

        log.debug("Extracting filters from query: {}", query);

        FilterConditions.FilterConditionsBuilder builder = FilterConditions.builder();

        // 가격 조건 추출
        MonthlyPriceCondition monthlyPriceCondition = extractPriceCondition(query);
        if (monthlyPriceCondition != null) {
            builder.monthlyPriceCondition(monthlyPriceCondition);
        }

        // 데이터 용량 조건 추출
        DataLimitCondition dataCondition = extractDataLimitCondition(query);
        if (dataCondition != null) {
            builder.mobileDataLimitMbCondition(dataCondition);
        }

        // 요금제 이름 추출
        String planName = extractPlanName(query);
        if (planName != null) {
            builder.planName(planName);
        }

        // 번들 혜택 추출
        List<String> bundledBenefits = extractBundledBenefits(query);
        builder.bundledBenefitNames(bundledBenefits);

        // 개별 혜택 추출
        List<String> singleBenefits = extractSingleBenefits(query);
        builder.singleBenefitNames(singleBenefits);

        // 혜택 유형 추출
        List<String> benefitTypes = extractBenefitTypes(query);
        builder.singleBenefitTypes(benefitTypes);

        return builder.build();
    }

    @Override
    public String generateCleanQuery(String originalQuery, FilterConditions filters) {
        String cleanQuery = originalQuery;

        // 가격 조건 제거
        cleanQuery = PRICE_PATTERN.matcher(cleanQuery).replaceAll("");

        // 데이터 용량 조건 제거
        cleanQuery = DATA_PATTERN.matcher(cleanQuery).replaceAll("");

        // 요금제 이름 제거
        cleanQuery = PLAN_NAME_PATTERN.matcher(cleanQuery).replaceAll("");

        // 특정 혜택 키워드 제거 (너무 구체적인 것들)
        for (String keyword : BUNDLED_BENEFIT_KEYWORDS) {
            cleanQuery = cleanQuery.replaceAll("(?i)" + Pattern.quote(keyword), "");
        }

        // 불필요한 공백 정리
        cleanQuery = cleanQuery.replaceAll("\\s+", " ").trim();

        log.debug("Generated clean query: {}", cleanQuery);
        return cleanQuery;
    }

    private MonthlyPriceCondition extractPriceCondition(String query) {

        Matcher matcher = PRICE_PATTERN.matcher(query);

        System.out.println("matcher = " + matcher);

        while (matcher.find()) {
            String amountStr = null;
            String operator = null;

            // 패턴에 따라 금액과 연산자 추출
            if (matcher.group(1) != null) {
                amountStr = matcher.group(1);
                operator = matcher.group(2);
            } else if (matcher.group(3) != null) {
                amountStr = matcher.group(3);
                operator = matcher.group(4);
            } else if (matcher.group(6) != null) {
                amountStr = matcher.group(6);
                operator = matcher.group(5);
            } else if (matcher.group(8) != null) {
                amountStr = matcher.group(8);
                operator = matcher.group(7);
            }

            if (amountStr != null && operator != null) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    // 만원 단위로 입력된 경우 변환
                    if (query.contains(amountStr + "만")) {
                        amount *= 10000;
                    }

                    ComparisonOperator compOp = mapToComparisonOperator(operator);
                    if (compOp != null) {
                        return MonthlyPriceCondition.builder()
                                .operator(compOp)
                                .amount(amount)
                                .build();
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse price amount: {}", amountStr);
                }
            }
        }

        return null;
    }

    private DataLimitCondition extractDataLimitCondition(String query) {

        if (query.contains("무제한")) {
            return DataLimitCondition.builder()
                    .operator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                    .limitMb(Integer.MAX_VALUE)
                    .build();
        }

        Matcher matcher = DATA_PATTERN.matcher(query);

        while (matcher.find()) {
            String amountStr = null;
            String unit = null;
            String operator = null;

            if (matcher.group(1) != null) {
                amountStr = matcher.group(1);
                unit = matcher.group(2);
                operator = matcher.group(3);
            } else if (matcher.group(5) != null) {
                amountStr = matcher.group(5);
                unit = matcher.group(6);
                operator = matcher.group(4);
            }

            if (amountStr != null && unit != null && operator != null) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    int limitMb = convertToMb(amount, unit);

                    ComparisonOperator compOp = mapToComparisonOperator(operator);
                    if (compOp != null) {
                        return DataLimitCondition.builder()
                                .operator(compOp)
                                .limitMb(limitMb)
                                .build();
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse data amount: {}", amountStr);
                }
            }
        }

        return null;
    }

    private String extractPlanName(String query) {

        Matcher matcher = PLAN_NAME_PATTERN.matcher(query);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1);
            } else if (matcher.group(3) != null) {
                return matcher.group(3);
            } else if (matcher.group(4) != null) {
                return matcher.group(4);
            }
        }

        return null;
    }

    private List<String> extractBundledBenefits(String query) {
        List<String> benefits = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (String keyword : BUNDLED_BENEFIT_KEYWORDS) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                benefits.add(keyword);
            }
        }

        return benefits;
    }

    private List<String> extractSingleBenefits(String query) {
        List<String> benefits = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (String keyword : SINGLE_BENEFIT_KEYWORDS) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                benefits.add(keyword);
            }
        }

        return benefits;
    }

    private List<String> extractBenefitTypes(String query) {
        List<String> types = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (String keyword : BENEFIT_TYPE_KEYWORDS) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                types.add(keyword);
            }
        }

        return types;
    }

    private ComparisonOperator mapToComparisonOperator(String operator) {
        return switch (operator.toLowerCase()) {
            case "이하", "이내", "아래" -> ComparisonOperator.LESS_THAN_OR_EQUAL;
            case "미만" -> ComparisonOperator.LESS_THAN;
            case "이상", "넘", "위" -> ComparisonOperator.GREATER_THAN_OR_EQUAL;
            case "초과" -> ComparisonOperator.GREATER_THAN;
            default -> null;
        };
    }

    private int convertToMb(double amount, String unit) {
        return switch (unit.toLowerCase()) {
            case "gb", "기가" -> (int) (amount * 1024);
            case "tb", "테라" -> (int) (amount * 1024 * 1024);
            default -> (int) amount;
        };
    }
}
