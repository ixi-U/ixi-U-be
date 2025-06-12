package com.ixi_U;

import com.ixi_U.common.AbstractNeo4jContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class IxiUApplicationTests extends AbstractNeo4jContainer {

    @Test
    void contextLoads() {
    }
}
