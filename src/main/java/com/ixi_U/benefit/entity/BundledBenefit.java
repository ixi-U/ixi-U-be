package com.ixi_U.benefit.entity;

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

    private final String description;

    private final int choice;

    @Builder.Default
    @Relationship(type = "BUNDLED", direction = Relationship.Direction.INCOMING)
    private List<SingleBenefit> singleBenefits = new ArrayList<>();

    public static BundledBenefit of(final String name, final String description, final int choice){

        return BundledBenefit.builder()
                .name(name)
                .description(description)
                .choice(choice)
                .build();
    }

    public void addSingleBenefit(final SingleBenefit singleBenefit){

        singleBenefits.add(singleBenefit);
    }
}

