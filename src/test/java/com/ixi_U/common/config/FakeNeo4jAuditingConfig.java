package com.ixi_U.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeNeo4jAuditingConfig {

    @Bean("neo4jAuditingHandler")
    public Object fakeAuditingHandler() {

        return new Object();
    }
}
