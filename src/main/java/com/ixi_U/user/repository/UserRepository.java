package com.ixi_U.user.repository;

import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends Neo4jRepository<User, String> {

    @Query("""
            MATCH (u:User)-[r:REVIEWED]->(p:Plan) where p.id = $planId
            RETURN round(avg(coalesce(r.point, 0)), 1) as averagePoint, count(r) as totalCount
            """)
    ShowReviewStatsResponse findAveragePointAndReviewCount(
            @Param("planId") String planId);

}
