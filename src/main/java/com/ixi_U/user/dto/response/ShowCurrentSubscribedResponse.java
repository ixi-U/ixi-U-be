package com.ixi_U.user.dto.response;


import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import java.util.List;

public record ShowCurrentSubscribedResponse(
        String name,
        Integer mobileDataLimitMb,
        Integer monthlyPrice,
        Double pricePerKb,
        List<BundledBenefit> bundledBenefits,
        List<SingleBenefit> singleBenefits
) {

    public static ShowCurrentSubscribedResponse of(
            String name,
            Integer mobileDataLimitMb,
            Integer monthlyPrice,
            Double pricePerKb,
            List<BundledBenefit> bundledBenefits,
            List<SingleBenefit> singleBenefits
    ) {
        return new ShowCurrentSubscribedResponse(
                name,
                mobileDataLimitMb,
                monthlyPrice,
                pricePerKb,
                bundledBenefits,
                singleBenefits);
    }
}