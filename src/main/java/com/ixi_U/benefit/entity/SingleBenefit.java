package com.ixi_U.benefit.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
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

    private final String subscript;

    private final BenefitType benefitType;

    public static SingleBenefit create(final String name, final String subscript, final BenefitType benefitType){

        return SingleBenefit.builder()
                .name(name)
                .subscript(subscript)
                .benefitType(benefitType)
                .build();
    }
}

