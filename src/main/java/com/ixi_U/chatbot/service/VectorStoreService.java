package com.ixi_U.chatbot.service;

import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import jakarta.validation.Valid;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final ChatBotService chatBotService;
    @Qualifier("planVectorStore")
    private final VectorStore planVectorStore;

    public PlanEmbeddedResponse saveEmbeddedPlan(@Valid GeneratePlanDescriptionRequest request) {

        String planDescription = chatBotService.getPlanDescription(request);

        log.info("planDescription = {}", planDescription);

        Map<String, Object> metadata = flattenMetadata(request);

        log.info("metadata = {}", metadata);

        planVectorStore.add(List.of(new Document(planDescription, metadata)));

        return PlanEmbeddedResponse.create(planDescription, metadata);
    }

    /**
     * 저장된 요금제 일괄 벡터 저장소에 저장
     */
    public void embedAllPlan(List<GeneratePlanDescriptionRequest> requests) {

        List<Document> documents = new ArrayList<>();

        for (GeneratePlanDescriptionRequest request : requests) {

            String planDescription = chatBotService.getPlanDescription(request);

            Map<String, Object> metadata = flattenMetadata(request);

            Document document = new Document(planDescription, metadata);

            documents.add(document);
        }

        planVectorStore.add(documents);
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

                if (value == null) {
                    continue;
                }

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
                    if (value.getClass().isEnum()) {
                        // Enum이면 name()으로 변환
                        result.put(fieldName, ((Enum<?>) value).name());
                    } else {
                        result.put(fieldName, value);
                    }
                }

            } catch (Exception e) {

                log.error("평탄화 에러 = {} : {}", field.getName(), e.getMessage());
            }
        }
        return result;
    }

    private void processBundledBenefitList(Map<String, Object> result,
            List<BundledBenefitDTO> bundledBenefits) {

        try {

            for (BundledBenefitDTO bundledBenefit : bundledBenefits) {

                result.put(bundledBenefit.name(), true);

                for (SingleBenefitDTO singleBenefit : bundledBenefit.singleBenefits()) {

                    result.put(singleBenefit.name(), true);
                    result.put(singleBenefit.benefitType().getType(), true);
                }
            }

        } catch (Exception e) {

            log.error("BundledBenefit 평탄화 오류 = {}", e.getMessage());
        }
    }

    private void processSingleBenefitList(Map<String, Object> result,
            List<SingleBenefitDTO> singleBenefits) {

        try {

            for (SingleBenefitDTO singleBenefit : singleBenefits) {

                result.put(singleBenefit.name(), true);
                result.put(singleBenefit.benefitType().getType(), true);
            }
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
                type.isEnum();
    }
}
