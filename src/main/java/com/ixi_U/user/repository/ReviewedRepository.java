package com.ixi_U.user.repository;

import com.ixi_U.user.entity.Reviewed;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewedRepository extends Neo4jRepository<Reviewed, Long> {

    @Query("""
            MATCH (u:User {id: $userId})-[r:REVIEWED]->(p:Plan {id: $planId})
            RETURN count(r) > 0
            """)
    boolean existsReviewedRelation(@Param("userId") String userId, @Param("planId") String planId);

}
