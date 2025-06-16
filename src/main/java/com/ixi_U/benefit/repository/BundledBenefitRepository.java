package com.ixi_U.benefit.repository;

import com.ixi_U.benefit.entity.BundledBenefit;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BundledBenefitRepository extends Neo4jRepository<BundledBenefit, String> {

}
