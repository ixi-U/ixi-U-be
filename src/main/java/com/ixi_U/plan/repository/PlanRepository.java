package com.ixi_U.plan.repository;

import com.ixi_U.plan.entity.Plan;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PlanRepository extends Neo4jRepository<Plan,String> {

}
