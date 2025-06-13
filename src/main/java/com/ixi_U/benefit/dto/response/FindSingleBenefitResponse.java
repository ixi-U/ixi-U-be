package com.ixi_U.benefit.dto.response;

import com.ixi_U.benefit.entity.BenefitType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindSingleBenefitResponse {

    private String id;
    private String name;
    private String subscript;
    private BenefitType benefitType;

    public static FindSingleBenefitResponse create(
            final String id,
            final String name,
            final String subscript,
            final BenefitType benefitType) {
        return FindSingleBenefitResponse.builder()
                .id(id)
                .name(name)
                .subscript(subscript)
                .benefitType(benefitType)
                .build();
    }
}
