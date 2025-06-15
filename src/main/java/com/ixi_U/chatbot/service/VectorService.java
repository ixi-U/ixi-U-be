package com.ixi_U.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private static final String SINGLE_BENEFIT_NAMES = "singleBenefitNames";
    private static final String BUNDLED_BENEFIT_NAMES = "bundledBenefitNames";
    private static final String SINGLE_BENEFIT_TYPES = "singleBenefitTypes";

    private final ChatBotService chatBotService;
    private final Neo4jVectorStore vectorStore;

    public PlanEmbeddedResponse saveEmbeddedPlan(@Valid GeneratePlanDescriptionRequest request) {

        String planDescription = chatBotService.getPlanDescription(request);

        log.info("planDescription = {}", planDescription);

        Map<String, Object> metadata = flattenMetadata(request);

        log.info("metadata = {}", metadata);

        vectorStore.add(List.of(new Document(planDescription, metadata)));

        return PlanEmbeddedResponse.create(planDescription, metadata);
    }

    /**
     * metadata 에 넣기 위한 평탄화 로직
     */
    private Map<String, Object> flattenMetadata(Object request) {

        Map<String, Object> result = new HashMap<>();

        // request 의 모든 필드 순차 탐색
        for (Field field : request.getClass().getDeclaredFields()) {

            field.setAccessible(true);

            try {

                Object value = field.get(request);

                if (value == null) continue;

                String fieldName = field.getName(); // id, name ...

                // List<BundledBenefitDTO> 처리
                if (value instanceof List<?> list && !list.isEmpty()) {

                    Object firstItem = list.get(0);

                    if (firstItem instanceof BundledBenefitDTO) {

                        processBundledBenefitList(result, (List<BundledBenefitDTO>) list);
                    }
                    // List<SingleBenefitDTO> 처리
                    else if (firstItem instanceof SingleBenefitDTO) {

                        processSingleBenefitList(result, (List<SingleBenefitDTO>) list);
                    }
                }
                // 기본 타입, 래퍼 클래스, String, Enum 처리
                else if (isPrimitiveOrWrapperOrStringOrEnum(value.getClass())) {

                    result.put(fieldName, value);
                }

            } catch (Exception e) {

                log.error("평탄화 에러 = {} : {}", field.getName(), e.getMessage());
            }
        }
        return result;
    }

    private void processBundledBenefitList(Map<String, Object> result, List<BundledBenefitDTO> bundledBenefits) {

        // List를 JSON 문자열로 변환하여 저장
        try {

            Set<String> names = new HashSet<>();
            Set<String> allSingleBenefitNames = new HashSet<>();
            Set<String> allSingleBenefitTypes = new HashSet<>();


            for (BundledBenefitDTO bundledBenefit : bundledBenefits) {

                names.add(bundledBenefit.name());

                // 각 BundledBenefit 안의 SingleBenefit들의 이름 수집
                for (SingleBenefitDTO singleBenefit : bundledBenefit.singleBenefits()) {

                    allSingleBenefitNames.add(singleBenefit.name());
                    allSingleBenefitTypes.add(singleBenefit.benefitType().getType());
                }
            }

            // 검색을 위한 단일 문자열 필드
            result.put(BUNDLED_BENEFIT_NAMES, names);
            result.put(SINGLE_BENEFIT_NAMES, allSingleBenefitNames);

            HashSet<String> previousValue = (HashSet<String>) result.getOrDefault(SINGLE_BENEFIT_TYPES, new HashSet<>());
            previousValue.addAll(allSingleBenefitTypes);
            result.put(SINGLE_BENEFIT_TYPES, previousValue);

        } catch (Exception e) {

            log.error("BundledBenefit 평탄화 오류 = {}", e.getMessage());
        }
    }

    private void processSingleBenefitList(Map<String, Object> result, List<SingleBenefitDTO> singleBenefits) {

        try {

            Set<String> names = new HashSet<>();
            Set<String> benefitTypes = new HashSet<>();

            for (SingleBenefitDTO singleBenefit : singleBenefits) {
                names.add(singleBenefit.name());
                benefitTypes.add(singleBenefit.benefitType().getType());
            }

            // 검색을 위한 단일 문자열
            HashSet<String> previousSingleBenefit = (HashSet<String>) result.getOrDefault(SINGLE_BENEFIT_NAMES, new HashSet<>());
            previousSingleBenefit.addAll(names);

            result.put(SINGLE_BENEFIT_NAMES, previousSingleBenefit);

            HashSet<String> previousValue = (HashSet<String>) result.getOrDefault(SINGLE_BENEFIT_TYPES, new HashSet<>());
            previousValue.addAll(benefitTypes);
            result.put(SINGLE_BENEFIT_TYPES, previousValue);

        } catch (Exception e) {

            log.error("SingleBenefit 평탄화 오류 = {}", e.getMessage());
        }
    }

    private boolean isPrimitiveOrWrapperOrStringOrEnum(Class<?> type) {

        return type.isPrimitive() ||
                type == Boolean.class ||
                type == Integer.class ||
                type == Character.class ||
                type == Byte.class ||
                type == Short.class ||
                type == Double.class ||
                type == Long.class ||
                type == Float.class ||
                type == String.class ||
                type.isEnum(); // Enum 타입 추가
    }
}
