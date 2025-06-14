package com.ixi_U.benefit.repository;

import com.ixi_U.benefit.entity.SingleBenefit;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleBenefitRepository extends Neo4jRepository<SingleBenefit, String> {

}
