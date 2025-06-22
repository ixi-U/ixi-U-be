package com.ixi_U.plan.entity;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanIndexInitializer {

    private final Neo4jClient neo4jClient;

    @PostConstruct
    public void createIndexes() {

        neo4jClient.query("""
                    CREATE INDEX plan_type_index IF NOT EXISTS FOR (p:Plan) ON (p.planType)
                """).run();

        log.info("요금제 목록 조회 인덱스 생성 완료");
    }
}
