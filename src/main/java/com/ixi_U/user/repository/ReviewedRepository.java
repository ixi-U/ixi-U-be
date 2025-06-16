package com.ixi_U.user.repository;

import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.entity.Reviewed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewedRepository extends Neo4jRepository<Reviewed, Long> {

    @Query("""
            RETURN EXISTS {
               MATCH (u:User {id: $userId})-[:REVIEWED]->(p:Plan {id: $planId})
            }
            """)
    boolean existsReviewedRelation(@Param("userId") String userId, @Param("planId") String planId);

    @Query("""
            MATCH (u:User)-[r:REVIEWED]->(p:Plan {id: $planId})
            RETURN id(r) as reviewId, r.comment as comment, u.name as userName, r.point as point, r.createdAt as createdAt
            :#{orderBy(#pageable)} SKIP $skip LIMIT $limit           
            """)
    Slice<ShowReviewResponse> findReviewedByPlanWithPaging(@Param("planId") String planId,
            Pageable pageable);

    @Query("""
           MATCH (u:User)-[r:REVIEWED]->(p:Plan)
           WHERE id(r) = $reviewId
           SET r.comment = $comment
           """)
    void updateReviewed(Long reviewId,String comment);

    @Query("""
           MATCH (u:User)-[r:REVIEWED]->(p:Plan)
           WHERE id(r) = $reviewId
           DELETE r
           """)
    void deleteReviewedById(Long reviewId);

    @Query("""
           MATCH (u:User {id: $userId})-[r:REVIEWED]->(p:Plan {id: $planId})
           RETURN id(r) as reviewId, r.comment as comment, u.name as userName, r.point as point, r.createdAt as createdAt
           """)
    ShowReviewResponse showMyReview(@Param("userId") String userId, @Param("planId") String planId);
}
