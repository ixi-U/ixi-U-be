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

    @Property(name = "name")
    private final String name;

    private final State state;

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

    public static Plan of(final String name) {

        return Plan.builder()
                .name(name)
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