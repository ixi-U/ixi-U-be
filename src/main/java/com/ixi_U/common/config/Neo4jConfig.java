package com.ixi_U.common.config;

import com.ixi_U.common.converter.PlanTypeReadingConverter;
import com.ixi_U.common.converter.PlanTypeWritingConverter;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;

@Configuration
public class Neo4jConfig {

    @Bean
    public Neo4jConversions neo4jConversions() {
        // 추가 컨버터를 한 번에 모두 등록
        Set<GenericConverter> converters = Set.of(
                new PlanTypeWritingConverter(),
                new PlanTypeReadingConverter()
        );
        return new Neo4jConversions(converters);
    }
}