package com.ixi_U.plan.repository;

import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.entity.Plan;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
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
}
