package com.ixi_U.user.entity;

import com.ixi_U.plan.entity.Plan;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Property;
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

    @CreatedDate
    @Property("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Property("updated_at")
    private LocalDateTime updatedAt;

    public static Reviewed of(final int point, final Plan plan, final String comment) {

        return Reviewed.builder()
                .point(point)
                .plan(plan)
                .comment(comment)
                .build();
    }
}
