package com.ixi_U.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.user.dto.request.OnboardingRequest;
import com.ixi_U.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @BeforeEach
    void setUpAuthentication() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("mockUserId", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("입력된 이메일과 요금제를 사용자의 DB에 저장 요청한다")
    void save_email_and_plan_to_user() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest("test@example.com", "요금제A");

        // when
        mockMvc.perform(post("/api/user/onboarding")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        verify(userService, times(1))
                .updateOnboardingInfo("mockUserId", "test@example.com", "요금제A");
    }
}