package com.ixi_U.plan.entity;

import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
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

    @Property(name = "name")
    private final String name;

    private final State state;

    private final int mobile_data_limit_mb;

    private final int shared_mobile_data_limit_mb;

    private final int call_limit_minutes;

    private final int message_limit;

    private final long monthly_price;

    private final PlanType plan_type;

    private final String usage_cautions;

    private final int mobile_data_throttle_speed_kbps;

    private final int min_age;

    private final int max_age;

    private final boolean is_active_duty;

    private final int price_per_kb;

    private final String etc_info;

    private final int priority;

    @Builder.Default
    @Relationship(type = "HAS_BENEFIT", direction = Relationship.Direction.OUTGOING)
    private List<BundledBenefit> bundledBenefits = new ArrayList<>();

    @Builder.Default
    @Relationship(type = "HAS_BENEFIT", direction = Relationship.Direction.OUTGOING)
    private List<SingleBenefit> singleBenefits = new ArrayList<>();

    public static Plan of(final String name, final int mobile_data_limit_mb,
            final int shared_mobile_data_limit_mb, final int call_limit_minutes,
            final int message_limit, final long monthly_price, final PlanType plan_type,
            final String usage_cautions, final int mobile_data_throttle_speed_kbps,
            final int min_age, final int max_age, final boolean is_active_duty,
            final int price_per_kb, final String etc_info, final int priority) {

        return Plan.builder()
                .name(name)
                .mobile_data_limit_mb(mobile_data_limit_mb)
                .shared_mobile_data_limit_mb(shared_mobile_data_limit_mb)
                .call_limit_minutes(call_limit_minutes)
                .message_limit(message_limit)
                .monthly_price(monthly_price)
                .plan_type(plan_type)
                .usage_cautions(usage_cautions)
                .mobile_data_throttle_speed_kbps(mobile_data_throttle_speed_kbps)
                .min_age(min_age)
                .max_age(max_age)
                .is_active_duty(is_active_duty)
                .price_per_kb(price_per_kb)
                .etc_info(etc_info)
                .priority(priority)
                .build();
    }

    public void addBundledBenefit(final BundledBenefit bundledBenefit) {

        bundledBenefits.add(bundledBenefit);
    }

    public void addSingleBenefit(final SingleBenefit singleBenefit) {

        singleBenefits.add(singleBenefit);
    }
}