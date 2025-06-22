package com.ixi_U;

import com.ixi_U.chatbot.advisor.ForbiddenWordAdvisor;
import com.ixi_U.common.AbstractNeo4jContainer;
import com.ixi_U.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class IxiUApplicationTests extends AbstractNeo4jContainer {

    @MockBean(name = "planVectorStore")
    private VectorStore planVectorStore;

    @MockBean
    private ForbiddenWordAdvisor forbiddenWordAdvisor;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
    }
}
