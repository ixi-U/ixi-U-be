package com.ixi_U.benefit.entity;

import java.time.Instant;
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
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node(value = "SingleBenefit")
@Getter
@With
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleBenefit {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    private final String name;

    private final String description;

    private final BenefitType benefitType;

    @CreatedDate
    @Property("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Property("updated_at")
    private Instant updatedAt;

    public static SingleBenefit of(final String name, final String description,
            final BenefitType benefitType) {

        return SingleBenefit.builder()
                .name(name)
                .description(description)
                .benefitType(benefitType)
                .build();
    }
}

