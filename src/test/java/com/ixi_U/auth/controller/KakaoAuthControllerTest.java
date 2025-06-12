package com.ixi_U.auth.controller;

import com.ixi_U.auth.exception.KakaoAuthException;
import com.ixi_U.auth.service.KakaoAuthService;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class KakaoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KakaoAuthService kakaoAuthService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 카카오_인가코드_정상일때_로그인_성공_및_JWT_쿠키반환() throws Exception {
        // given
        String code = "sample_auth_code";
        String jwt = jwtTokenProvider.generateToken("user-id-123", "ROLE_USER");

        when(kakaoAuthService.loginAndIssueJwt(code)).thenReturn(jwt);

        // when & then
        mockMvc.perform(get("/login/auth/code/kakao")
                        .param("code", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/login/status"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().value("access_token", jwt));
    }

    @Test
    void 인가코드_유효하지않으면_에러쿼리포함된_리다이렉트반환() throws Exception {
        // given
        String invalidCode = "invalid_code";
        when(kakaoAuthService.loginAndIssueJwt(invalidCode))
                .thenThrow(new GeneralException(KakaoAuthException.TOKEN_ISSUE_FAILED));

        // when & then
        mockMvc.perform(get("/login/auth/code/kakao")
                        .param("code", invalidCode))
                .andExpect(status().isFound()) // 302 redirect
                .andExpect(header().string("Location",
                        "http://localhost:3000/login/status?error=TOKEN_ISSUE_FAILED"));
    }
}