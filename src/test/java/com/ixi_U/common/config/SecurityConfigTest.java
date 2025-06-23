package com.ixi_U.common.config;

import com.ixi_U.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@WebMvcTest(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private com.ixi_U.jwt.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.ixi_U.user.repository.UserRepository userRepository;

    @MockBean
    private com.ixi_U.auth.service.CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("AuthorizationRequestRepository Bean은 HttpCookieOAuth2AuthorizationRequestRepository여야 한다")
    void authorizationRequestRepositoryBeanIsCookieBased() {
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> repo = securityConfig.authorizationRequestRepository();

        assertThat(repo).isInstanceOf(HttpCookieOAuth2AuthorizationRequestRepository.class);
    }
}