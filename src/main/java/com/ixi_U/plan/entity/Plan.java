package com.ixi_U.plan.entity;

import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node
@Getter
@With
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    private final String name;

    private final PlanState planState;

    private final int mobileDataLimitMb;

    private final int sharedMobileDataLimitMb;

    private final int callLimitMinutes;

    private final int messageLimit;

    private final int monthlyPrice;

    private final PlanType planType;

    private final String usageCautions;

    private final int mobileDataThrottleSpeedKbps;

    private final int minAge;

    private final int maxAge;

    private final boolean isActiveDuty;

    private final int pricePerKb;

    private final String etcInfo;

    private final int priority;

    @CreatedDate
    @Property("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Property("updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Relationship(type = "HAS_BENEFIT", direction = Relationship.Direction.OUTGOING)
    private List<BundledBenefit> bundledBenefits = new ArrayList<>();

    @Builder.Default
    @Relationship(type = "HAS_BENEFIT", direction = Relationship.Direction.OUTGOING)
    private List<SingleBenefit> singleBenefits = new ArrayList<>();

    public static Plan of(

            final String name,
            final int mobileDataLimitMb,
            final int sharedMobileDataLimitMb,
            final int callLimitMinutes,
            final int messageLimit,
            final int monthlyPrice,
            final PlanType planType,
            final String usageCautions,
            final int mobileDataThrottleSpeedKbps,
            final int minAge,
            final int maxAge,
            final boolean isActiveDuty,
            final int pricePerKb,
            final String etcInfo,
            final int priority,
            List<BundledBenefit> bundledBenefits,
            List<SingleBenefit> singleBenefits
    ) {
        return Plan.builder()
                .name(name)
                .planState(PlanState.ABLE)
                .mobileDataLimitMb(mobileDataLimitMb)
                .sharedMobileDataLimitMb(sharedMobileDataLimitMb)
                .callLimitMinutes(callLimitMinutes)
                .messageLimit(messageLimit)
                .monthlyPrice(monthlyPrice)
                .planType(planType)
                .usageCautions(usageCautions)
                .mobileDataThrottleSpeedKbps(mobileDataThrottleSpeedKbps)
                .minAge(minAge)
                .maxAge(maxAge)
                .isActiveDuty(isActiveDuty)
                .pricePerKb(pricePerKb)
                .etcInfo(etcInfo)
                .priority(priority)
                .bundledBenefits(bundledBenefits)
                .singleBenefits(singleBenefits)
                .build();
    }

    public void addBundledBenefit(final BundledBenefit bundledBenefit) {

        bundledBenefits.add(bundledBenefit);
    }

    public void addSingleBenefit(final SingleBenefit singleBenefit) {

        singleBenefits.add(singleBenefit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {

            return true;
        }
        if (o == null || getClass() != o.getClass()) {

            return false;
        }
        Plan plan = (Plan) o;
        if (id == null || plan.id == null) {

            return false;
        }

        return id.equals(plan.id);
    }

    @Override
    public int hashCode() {

        return id == null ? 0 : id.hashCode();
    }

}
