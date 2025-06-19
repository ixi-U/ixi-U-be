package com.ixi_U;

import com.ixi_U.common.AbstractNeo4jContainer;
import com.ixi_U.security.jwt.provider.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class IxiUApplicationTests extends AbstractNeo4jContainer {

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void contextLoads() {
    }
}
