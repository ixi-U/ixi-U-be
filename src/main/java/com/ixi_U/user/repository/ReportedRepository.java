package com.ixi_U.user.repository;

import com.ixi_U.user.entity.Reported;
import com.ixi_U.user.entity.Reviewed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface ReportedRepository extends Neo4jRepository<Reported, String> {

    @Query("""
           MATCH (r:Reported)
           RETURN r
           ORDER BY r.createdAt DESC SKIP $skip LIMIT $limit
           """)
    Slice<Reported> findReportedWithSlice(Pageable pageable);
}
