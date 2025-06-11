package com.ixi_U;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;

@EnableNeo4jAuditing
@SpringBootApplication
public class IxiUApplication {

    public static void main(String[] args) {
        SpringApplication.run(IxiUApplication.class, args);
    }

}
