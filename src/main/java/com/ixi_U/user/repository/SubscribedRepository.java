package com.ixi_U.user.repository;

import com.ixi_U.user.entity.Subscribed;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface SubscribedRepository extends Neo4jRepository<Subscribed, Long> {

    @Query("""
            RETURN EXISTS {
               MATCH (u:User {id: $userId})-[:SUBSCRIBED]->(p:Plan {id: $planId})
            }
            """)
    boolean existsSubscribeRelation(@Param("userId") String userId, @Param("planId") String planId);
}
