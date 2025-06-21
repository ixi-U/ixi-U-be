package com.ixi_U.user.entity;

import com.ixi_U.plan.entity.Plan;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node
@Getter
@With
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Reported {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    private Long reviewedId;

    @CreatedDate
    private LocalDateTime createdAt;

    public static Reported from(final Long reviewedId) {

        return Reported.builder()
                .reviewedId(reviewedId)
                .build();

    }
}
