package com.ixi_U.plan.repository;

import com.ixi_U.chatbot.tool.dto.MostReviewPointPlanToolDto;
import com.ixi_U.chatbot.tool.dto.MostReviewedPlanToolDto;
import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.entity.Plan;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends Neo4jRepository<Plan, String>, PlanCustomRepository {

    boolean existsByName(String name);

    @Query("MATCH (p:Plan) RETURN p")
    List<Plan> findAllForAdmin();

//    @Query("""
//        MATCH (p:Plan)
//        RETURN p {
//            .id,
//            .name,
//            .planState,
//            .usageCautions
//        } AS p
//        """)
//    List<Plan> findAllForAdmin();

    Optional<Plan> findByName(String name);

    @Query("""
    MATCH (p:Plan)
    RETURN p.id AS id, p.name AS name
    """)
    List<PlanNameDto> findAllPlanNames();

    /**
     * 가장 많은 리뷰를 받은 요금제 조회
     */
    @Query("MATCH (u:User)-[r:REVIEWED]->(p:Plan) " +
            "OPTIONAL MATCH (p)-[:HAS_BENEFIT]->(sb:SingleBenefit) " +
            "OPTIONAL MATCH (p)-[:HAS_BENEFIT]->(bb:BundledBenefit) " +
            "WITH p, COUNT(DISTINCT r) as reviewedCount, " +
            "     COLLECT(DISTINCT {id: sb.id, name: sb.name, subscript: sb.subscript}) as singleBenefits, " +
            "     COLLECT(DISTINCT {id: bb.id, name: bb.name, subscript: bb.subscript}) as bundledBenefits " +
            "RETURN p.id as id, " +
            "       p.name as name, " +
            "       p.mobileDataLimitMb as mobileDataLimitMb, " +
            "       p.sharedMobileDataLimitMb as sharedMobileDataLimitMb, " +
            "       p.callLimitMinutes as callLimitMinutes, " +
            "       p.messageLimit as messageLimit, " +
            "       p.monthlyPrice as monthlyPrice, " +
            "       p.priority as priority, " +
            "       reviewedCount, " +
            "       singleBenefits, " +
            "       bundledBenefits " +
            "ORDER BY reviewedCount DESC " +
            "SKIP $skip LIMIT 1")
    MostReviewedPlanToolDto findMostReviewedPlan(@Param("skip") int skip);

    /**
     * 가장 높은 리뷰 점수를 받은 요금제 조회
     */
    @Query("MATCH (u:User)-[r:REVIEWED]->(p:Plan) " +
            "OPTIONAL MATCH (p)-[:HAS_BENEFIT]->(sb:SingleBenefit) " +
            "OPTIONAL MATCH (p)-[:HAS_BENEFIT]->(bb:BundledBenefit) " +
            "WITH p, AVG(r.point) as reviewPointAverage, " +
            "     COLLECT(DISTINCT {id: sb.id, name: sb.name, subscript: sb.subscript}) as singleBenefits, " +
            "     COLLECT(DISTINCT {id: bb.id, name: bb.name, subscript: bb.subscript}) as bundledBenefits " +
            "RETURN p.id as id, " +
            "       p.name as name, " +
            "       p.mobileDataLimitMb as mobileDataLimitMb, " +
            "       p.sharedMobileDataLimitMb as sharedMobileDataLimitMb, " +
            "       p.callLimitMinutes as callLimitMinutes, " +
            "       p.messageLimit as messageLimit, " +
            "       p.monthlyPrice as monthlyPrice, " +
            "       p.priority as priority, " +
            "       reviewPointAverage, " +
            "       singleBenefits, " +
            "       bundledBenefits " +
            "ORDER BY reviewPointAverage DESC " +
            "SKIP $skip LIMIT 1")
    MostReviewPointPlanToolDto findMostReviewPointPlan(@Param("skip") int skip);
}
