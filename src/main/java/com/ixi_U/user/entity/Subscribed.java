package com.ixi_U.user.entity;

import com.ixi_U.plan.entity.Plan;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
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

    @CreatedDate
    private LocalDateTime createdAt;

    public static Subscribed of(final Plan plan) {

        return Subscribed.builder()
                .plan(plan)
                .createdAt(LocalDateTime.now())
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
