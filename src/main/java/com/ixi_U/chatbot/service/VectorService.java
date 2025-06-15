package com.ixi_U.chatbot.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private static final String DTO = "DTO";
    private static final String REQUEST = "Request";
    private static final String NAMES = "Names";
    private static final String COUNT = "Count";
    private static final String COMMA = ", ";
    private static final String SINGLE_BENEFIT_NAMES = "SingleBenefitNames";
    private static final String BENEFIT_TYPES = "BenefitTypes";
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

                        processBundledBenefitList(result, fieldName, (List<BundledBenefitDTO>) list);
                    }
                    // List<SingleBenefitDTO> 처리
                    else if (firstItem instanceof SingleBenefitDTO) {

                        processSingleBenefitList(result, fieldName, (List<SingleBenefitDTO>) list);
                    }
                    // 기타 DTO 리스트 처리 (name 필드가 있는 경우)
                    else if (isDto(firstItem)) {

                        processGenericDtoList(result, fieldName, list);
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

    private void processBundledBenefitList(Map<String, Object> result, String fieldName, List<BundledBenefitDTO> bundledBenefits) {

        // List를 JSON 문자열로 변환하여 저장
        try {

            List<String> names = new ArrayList<>();
            List<String> allSingleBenefitNames = new ArrayList<>();

            for (BundledBenefitDTO bundledBenefit : bundledBenefits) {

                names.add(bundledBenefit.name());

                // 각 BundledBenefit 안의 SingleBenefit들의 이름 수집
                for (SingleBenefitDTO singleBenefit : bundledBenefit.singleBenefits()) {

                    allSingleBenefitNames.add(singleBenefit.name());
                }
            }

            // 검색을 위한 단일 문자열 필드
            result.put(fieldName + NAMES, String.join(COMMA, names));
            result.put(fieldName + SINGLE_BENEFIT_NAMES, String.join(COMMA, allSingleBenefitNames));
            result.put(fieldName + COUNT, bundledBenefits.size());

        } catch (Exception e) {

            log.error("BundledBenefit 평탄화 오류 = {}", e.getMessage());
        }
    }

    private void processSingleBenefitList(Map<String, Object> result, String fieldName, List<SingleBenefitDTO> singleBenefits) {

        try {

            List<String> names = new ArrayList<>();
            List<String> benefitTypes = new ArrayList<>();

            for (SingleBenefitDTO singleBenefit : singleBenefits) {
                names.add(singleBenefit.name());
                benefitTypes.add(singleBenefit.benefitType().name());
            }

            // 검색을 위한 단일 문자열 필드
            result.put(fieldName + NAMES, String.join(COMMA, names));
            result.put(fieldName + BENEFIT_TYPES, String.join(COMMA, benefitTypes));
            result.put(fieldName + COUNT, singleBenefits.size());

        } catch (Exception e) {

            log.error("SingleBenefit 평탄화 오류 = {}", e.getMessage());
        }
    }

    private void processGenericDtoList(Map<String, Object> result, String fieldName, List<?> list) {

        List<String> names = new ArrayList<>();

        for (Object item : list) {

            try {

                Field nameField = item.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                Object nameValue = nameField.get(item);

                if (nameValue instanceof String) {

                    names.add((String) nameValue);
                }
            } catch (Exception e) {

                // name 필드가 없는 경우 무시
            }
        }

        if (!names.isEmpty()) {

            try {

                result.put(fieldName + NAMES, String.join(COMMA, names));
                result.put(fieldName + COUNT, names.size());
            } catch (Exception e) {

                log.error("일반 평탄화 오류 = {}", e.getMessage());
            }
        }
    }

    private boolean isDto(Object obj) {

        if (obj == null) return false;

        String className = obj.getClass().getSimpleName();
        // DTO 판별 조건
        return className.endsWith(DTO) || className.endsWith(REQUEST);
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
