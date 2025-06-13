package com.ixi_U.benefit.entity;

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
public class BundledBenefit {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    private final String name;

    private final String subscript;

    private final int choice;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    @Relationship(type = "BUNDLED", direction = Relationship.Direction.INCOMING)
    private List<SingleBenefit> singleBenefits = new ArrayList<>();

    public static BundledBenefit create(final String name, final String subscript, final int choice){

        return BundledBenefit.builder()
                .name(name)
                .subscript(subscript)
                .choice(choice)
                .build();
    }

    public void addAllSingleBenefit(final List<SingleBenefit> newSingleBenefits){

        singleBenefits.addAll(newSingleBenefits);
    }
}

