package com.ixi_U.plan.repository;

import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.entity.SortOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Relationship;
import org.neo4j.cypherdsl.core.Statement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlanRepositoryImpl implements PlanCustomRepository {

    private final Neo4jTemplate neo4jTemplate;

    public Slice<PlanSummaryDto> findPlans(Pageable pageable, PlanType planType,
            PlanSortOption planSortOption, String searchKeyword, String planId,
            Integer cursorSortValue) {

        int limit = pageable.getPageSize();
        Map<String, Object> params = buildParams(planType, searchKeyword, planId, cursorSortValue);
        Condition condition = buildCondition(planSortOption, searchKeyword, planId,
                cursorSortValue);

        Node p = Cypher.node("Plan").named("p");
        Node b = Cypher.node("SingleBenefit").named("b");
        Relationship relationship = p.relationshipTo(b, "HAS_BENEFIT");

        Statement statement = Cypher.match(p)
                .where(condition)
                .optionalMatch(relationship)
                .with(p, Cypher.collect(Cypher.mapOf(
                        "id", b.property("id"),
                        "name", b.property("name"),
                        "description", b.property("description")
                )).as("singleBenefits"))
                .returning(
                        p.property("id").as("id"),
                        p.property("name").as("name"),
                        p.property("mobileDataLimitMb").as("mobileDataLimitMb"),
                        p.property("sharedMobileDataLimitMb").as("sharedMobileDataLimitMb"),
                        p.property("callLimitMinutes").as("callLimitMinutes"),
                        p.property("messageLimit").as("messageLimit"),
                        p.property("monthlyPrice").as("monthlyPrice"),
                        p.property("priority").as("priority"),
                        Cypher.name("singleBenefits")
                )
                .orderBy(planSortOption.getOrder() == SortOrder.ASC
                                ? p.property(planSortOption.getField()).ascending()
                                : p.property(planSortOption.getField()).descending(),
                        p.property("id").ascending())
                .limit(limit + 1)
                .build();

        List<PlanSummaryDto> content = neo4jTemplate.findAll(statement, params,
                PlanSummaryDto.class);

        boolean hasNext = content.size() > limit;
        if (hasNext) {
            content = content.subList(0, limit);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private Map<String, Object> buildParams(PlanType planType, String searchKeyword, String planId,
            Integer cursorSortValue) {

        Map<String, Object> params = new HashMap<>();
        params.put("planType", planType.name());

        if (planId != null && cursorSortValue != null) {
            params.put("cursorSortValue", cursorSortValue);
            params.put("planId", planId);
        }
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            params.put("searchKeyword", searchKeyword);
        }

        return params;
    }

    private Condition buildCondition(PlanSortOption planSortOption, String searchKeyword,
            String planId, Integer cursorSortValue) {

        Node p = Cypher.node("Plan").named("p");
        Condition condition = p.property("planType").eq(Cypher.parameter("planType"));

        if (planId != null && cursorSortValue != null) {
            Condition sortCondition = p.property(planSortOption.getField())
                    .lt(Cypher.parameter("cursorSortValue"))
                    .or(p.property(planSortOption.getField())
                            .eq(Cypher.parameter("cursorSortValue"))
                            .and(p.property("id").gt(Cypher.parameter("planId"))));
            condition = condition.and(sortCondition);
        }

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            condition = condition.and(
                    p.property("name").contains(Cypher.parameter("searchKeyword")));
        }

        return condition;
    }
}
