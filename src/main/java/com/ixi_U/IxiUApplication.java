package com.ixi_U;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
        Neo4jVectorStoreAutoConfiguration.class})
public class IxiUApplication {

    public static void main(String[] args) {
        SpringApplication.run(IxiUApplication.class, args);
    }

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

}
