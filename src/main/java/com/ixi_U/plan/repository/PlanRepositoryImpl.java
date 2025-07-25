package com.ixi_U.plan.repository;

import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.PlansCountDto;
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
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlanRepositoryImpl implements PlanCustomRepository {

    private final Neo4jTemplate neo4jTemplate;
    private final Neo4jClient neo4jClient;

    public Slice<PlanSummaryDto> findPlans(Pageable pageable, PlanType planType,
            PlanSortOption planSortOption, String searchKeyword, String planId,
            Integer cursorSortValue) {

        int limit = pageable.getPageSize();
        Map<String, Object> params = buildParams(planType, searchKeyword, planId, cursorSortValue);
        Condition condition = buildCondition(planType, planSortOption, searchKeyword, planId,
                cursorSortValue);

        Node p = Cypher.node("Plan").named("p");
        Node sb = Cypher.node("SingleBenefit").named("sb");
        Node bb = Cypher.node("BundledBenefit").named("bb");
        Relationship singleRelationship = p.relationshipTo(sb, "HAS_BENEFIT");
        Relationship bundledRelationship = p.relationshipTo(bb, "HAS_BENEFIT");

        Statement statement = Cypher.match(p)
                .where(condition)
                .optionalMatch(singleRelationship)
                .with(p, Cypher.collect(Cypher.mapOf(
                        "id", sb.property("id"),
                        "name", sb.property("name")
                )).as("singleBenefits"))
                .optionalMatch(bundledRelationship)
                .with(p,
                        Cypher.name("singleBenefits"),
                        Cypher.collect(Cypher.mapOf(
                                "id", bb.property("id"),
                                "name", bb.property("name")
                        )).as("bundledBenefits"))
                .withDistinct(p, Cypher.name("singleBenefits"), Cypher.name("bundledBenefits"))
                .returning(
                        p.property("id").as("id"),
                        p.property("name").as("name"),
                        p.property("mobileDataLimitMb").as("mobileDataLimitMb"),
                        p.property("sharedMobileDataLimitMb").as("sharedMobileDataLimitMb"),
                        p.property("callLimitMinutes").as("callLimitMinutes"),
                        p.property("messageLimit").as("messageLimit"),
                        p.property("monthlyPrice").as("monthlyPrice"),
                        p.property("priority").as("priority"),
                        Cypher.name("singleBenefits"),
                        Cypher.name("bundledBenefits").as("bundledBenefits"))
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

        if (planType != null) {
            params.put("planType", planType.name());
        }

        if (planId != null && cursorSortValue != null) {
            params.put("cursorSortValue", cursorSortValue);
            params.put("planId", planId);
        }
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            params.put("searchKeyword", searchKeyword);
        }

        return params;
    }

    private Condition buildCondition(PlanType planType, PlanSortOption planSortOption,
            String searchKeyword,
            String planId, Integer cursorSortValue) {

        Node p = Cypher.node("Plan").named("p");
        Condition condition = p.property("planState").ne(Cypher.literalOf("DISABLE"));

        if (planType != null) {
            condition = condition.and(
                    p.property("planType").eq(Cypher.parameter("planType"))
            );
        }
        if (planId != null && cursorSortValue != null) {
            Condition sortCondition;
            if (planSortOption.getOrder() == SortOrder.ASC) {
                sortCondition = p.property(planSortOption.getField())
                        .gt(Cypher.parameter("cursorSortValue"))
                        .or(p.property(planSortOption.getField())
                                .eq(Cypher.parameter("cursorSortValue"))
                                .and(p.property("id").gt(Cypher.parameter("planId"))));
            } else {
                sortCondition = p.property(planSortOption.getField())
                        .lt(Cypher.parameter("cursorSortValue"))
                        .or(p.property(planSortOption.getField())
                                .eq(Cypher.parameter("cursorSortValue"))
                                .and(p.property("id").gt(Cypher.parameter("planId"))));
            }
            condition = condition.and(sortCondition);
        }

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            condition = condition.and(
                    p.property("name").contains(Cypher.parameter("searchKeyword")));
        }

        return condition;
    }

    public PlansCountDto countPlans() {

        String cypher = """
                    MATCH (p:Plan)
                    WHERE p.planState <> 'DISABLE'
                    RETURN 'ALL' AS type, count(p) AS count
                    UNION
                    MATCH (p:Plan)
                    WHERE p.planState <> 'DISABLE' AND p.planType = 'FIVE_G_LTE'
                    RETURN 'FIVE_G_LTE' AS type, count(p) AS count
                    UNION
                    MATCH (p:Plan)
                    WHERE p.planState <> 'DISABLE' AND p.planType = 'ONLINE'
                    RETURN 'ONLINE' AS type, count(p) AS count
                    UNION
                    MATCH (p:Plan)
                    WHERE p.planState <> 'DISABLE' AND p.planType = 'TABLET_SMARTWATCH'
                    RETURN 'TABLET_SMARTWATCH' AS type, count(p) AS count
                    UNION
                    MATCH (p:Plan)
                    WHERE p.planState <> 'DISABLE' AND p.planType = 'DUAL_NUMBER'
                    RETURN 'DUAL_NUMBER' AS type, count(p) AS count
                """;

        List<Map<String, Object>> result = (List<Map<String, Object>>) neo4jClient.query(cypher)
                .fetch().all();

        int all = 0, fiveGLte = 0, online = 0, tabletSmartwatch = 0, dualNumber = 0;

        for (Map<String, Object> row : result) {
            String type = (String) row.get("type");
            Long count = (Long) row.get("count");

            switch (type) {
                case "ALL" -> all = count.intValue();
                case "FIVE_G_LTE" -> fiveGLte = count.intValue();
                case "ONLINE" -> online = count.intValue();
                case "TABLET_SMARTWATCH" -> tabletSmartwatch = count.intValue();
                case "DUAL_NUMBER" -> dualNumber = count.intValue();
            }
        }

        return new PlansCountDto(all, fiveGLte, online, tabletSmartwatch, dualNumber);
    }
}