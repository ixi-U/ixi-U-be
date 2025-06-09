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
public class Subscribed {

    @RelationshipId
    private final Long id;

    @TargetNode
    private final Plan plan;

    public static Subscribed of(final Plan plan){

        return Subscribed.builder()
                .plan(plan)
                .build();
    }
}