package com.ixi_U.auth.controller;

import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.common.config.SecurityConfig;
import com.ixi_U.jwt.JwtAuthenticationFilter;
import com.ixi_U.jwt.JwtTokenProvider
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {AuthController.class})
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String LOGOUT_URL = "/api/auth/logout";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("🔓 로그아웃 요청 시")
    class Logout {

        @Nested
        @DisplayName("유효한 access_token이 있을 때")
        class WhenTokenIsValid {

            @Test
            @DisplayName("로그아웃 성공")
            void logoutSuccessWithValidToken() throws Exception {
                // given
                String fakeAccessToken = "valid-token";
                String userId = "user123";

                MockCookie accessCookie = new MockCookie("access_token", fakeAccessToken);
                accessCookie.setPath("/");
                accessCookie.setHttpOnly(true);

                when(jwtTokenProvider.validateToken(fakeAccessToken)).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(fakeAccessToken)).thenReturn(userId);

                // when & then
                mockMvc.perform(post(LOGOUT_URL)
                                .cookie(accessCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string("로그아웃 완료"));

                verify(userService, times(1)).removeRefreshToken(userId);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 access_token일 때")
        class WhenTokenIsNotValid {

            @Test
            @DisplayName("로그아웃 실패")
            void logoutFailWithInvalidToken() throws Exception {

                // given
                String invalidToken = "invalid-token";
                MockCookie cookie = new MockCookie("access_token", invalidToken);
                cookie.setHttpOnly(true);
                cookie.setPath("/");

                when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

                // when & then
                mockMvc.perform(post(LOGOUT_URL)
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().string("유효하지 않은 토큰입니다."));

                verify(userService, never()).removeRefreshToken(anyString());
            }
        }

        @Nested
        @DisplayName("access_token이 없을 때")
        class WhenRefreshTokenIsMissing {

            @Test
            @DisplayName("access_token이 없을 때 로그아웃 실패")
            void logoutFailWhenNoAccessToken () throws Exception {

                // given

                // when & then
                mockMvc.perform(post(LOGOUT_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().string("유효하지 않은 토큰입니다."));

                verify(userService, never()).removeRefreshToken(anyString());
            }
        }
    }
}
