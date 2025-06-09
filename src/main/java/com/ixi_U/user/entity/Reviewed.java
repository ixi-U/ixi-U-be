package com.ixi_U.user.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.neo4j.driver.summary.Plan;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@With
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Reviewed {

    @RelationshipId
    private final Long id;

    private final int point;

    private final String comment;

    @TargetNode
    private final Plan plan;

    public static Reviewed of(final int point, final Plan plan) {

        return Reviewed.builder()
                .point(point)
                .plan(plan)
                .build();
    }
}
