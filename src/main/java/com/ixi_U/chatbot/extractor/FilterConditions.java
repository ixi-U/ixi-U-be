package com.ixi_U.chatbot.extractor;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterConditions {

    // 가격 조건
    private MonthlyPriceCondition monthlyPriceCondition;

    // 데이터 용량 조건
    private DataLimitCondition mobileDataLimitMbCondition;

    // 번들 혜택 (BundledBenefitNames)
    @Builder.Default
    private List<String> bundledBenefitNames = new ArrayList<>();

    // 개별 혜택 (SingleBenefitNames)
    @Builder.Default
    private List<String> singleBenefitNames = new ArrayList<>();

    // 혜택 유형 (SingleBenefitTypes)
    @Builder.Default
    private List<String> singleBenefitTypes = new ArrayList<>();

    // 요금제 이름
    private String planName;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FilterConditions{");

        if (monthlyPriceCondition != null) {
            sb.append("monthlyPrice=").append(monthlyPriceCondition).append(", ");
        }

        if (mobileDataLimitMbCondition != null) {
            sb.append("mobileDataLimitMb=").append(mobileDataLimitMbCondition).append(", ");
        }

        if (!bundledBenefitNames.isEmpty()) {
            sb.append("bundledBenefitNames=").append(bundledBenefitNames).append(", ");
        }

        if (!singleBenefitNames.isEmpty()) {
            sb.append("singleBenefitNames=").append(singleBenefitNames).append(", ");
        }

        if (!singleBenefitTypes.isEmpty()) {
            sb.append("singleBenefitTypes=").append(singleBenefitTypes).append(", ");
        }

        if (planName != null && !planName.trim().isEmpty()) {
            sb.append("planName='").append(planName).append("', ");
        }

        if (sb.length() > 17) {
            sb.setLength(sb.length() - 2); // 마지막 ", " 제거
        }

        sb.append("}");
        return sb.toString();
    }
}