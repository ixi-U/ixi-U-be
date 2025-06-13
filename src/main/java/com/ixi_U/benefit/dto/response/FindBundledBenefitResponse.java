package com.ixi_U.benefit.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindBundledBenefitResponse {

    private String id;
    private String name;
    private String subscript;
    private Integer choice;
    private List<FindSingleBenefitResponse> singleBenefitResponses;

    public static FindBundledBenefitResponse create(
            final String id,
            final String name,
            final String subscript,
            final Integer choice,
            final List<FindSingleBenefitResponse> singleBenefitResponses) {
        return FindBundledBenefitResponse.builder()
                .id(id)
                .name(name)
                .choice(choice)
                .subscript(subscript)
                .singleBenefitResponses(singleBenefitResponses)
                .build();
    }
}
