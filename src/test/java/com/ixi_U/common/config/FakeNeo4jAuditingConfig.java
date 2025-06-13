package com.ixi_U.common.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.AuditingHandler;

@TestConfiguration
public class FakeNeo4jAuditingConfig {

    @Bean("neo4jAuditingHandler")
    public AuditingHandler fakeAuditingHandler() {

        return Mockito.mock(AuditingHandler.class);
    }
}
