package com.ixi_U.user.entity;

import com.ixi_U.common.entity.BaseEntity;
import com.ixi_U.plan.entity.Plan;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@With
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscribed extends BaseEntity {

    @RelationshipId
    private final Long id;

    @TargetNode
    private final Plan plan;

    public static Subscribed of(final Plan plan) {

        return Subscribed.builder()
                .plan(plan)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subscribed that = (Subscribed) o;
        if (id == null || that.id == null) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

}