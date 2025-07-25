package com.ixi_U.user.repository;

import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndProvider(String nickname, String provider);

    @Query("""
            MATCH (u:User)
            WHERE u.kakaoId = $kakaoId
            RETURN u;
            """)
    Optional<User> findByKakaoId(@Param("kakaoId") Long kakaoId);

    @Query("""
            MATCH (u:User)-[r:REVIEWED]->(p:Plan) where p.id = $planId
            RETURN coalesce(round(avg(coalesce(r.point, 0)), 1), 0.0) as averagePoint,count(r) as totalCount
            """)
    ShowReviewStatsResponse findAveragePointAndReviewCount(
            @Param("planId") String planId);

    @Query("""
            MATCH (u:User)-[r:REVIEWED]->(p:Plan) where id(r) = $reviewedId
            RETURN u
            """)
    Optional<User> findOwnerByReviewedId(Long reviewedId);

    @Query("""
            MATCH (u:User)-[r:REVIEWED]->(p:Plan) where id(r) = $reviewedId
            RETURN u,r,p
            """)
    Optional<User> findOwnerAndReviewByReviewedId(Long reviewedId);

}
