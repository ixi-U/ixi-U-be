package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.common.config.SecurityConfig;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.List;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(value = {UserController.class})
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
class UserControllerTest {

    private static final String USER_URL = "/api/user";

    @MockBean
    UserService userService;
    @MockBean
    JwtTokenProvider oAuth2SuccessHandler;
    @MockBean
    CustomOAuth2UserService customOAuth2UserService;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    @MockBean
    UserRepository userRepository;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("유저의 나의 요금제 조회 요청은")
    class DescribeMyPlan {

        @BeforeEach
        public void initSecurity() {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("userId", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("성공적으로 나의 요금제를 조회할 수 있다")
        void it_returns_my_plan() throws Exception {

            //given
            ShowCurrentSubscribedResponse response1 = ShowCurrentSubscribedResponse.of(
                    "5G 프리미엄 요금제",
                    200_000,
                    85_000,
                    5,
                    List.of(
                            BundledBenefit.create("스트리밍 혜택", "넷플릭스 포함", 1)
                    ),
                    List.of(
                            SingleBenefit.create("데이터 리필 1GB", "월 1회 제공", BenefitType.SUBSCRIPTION),
                            SingleBenefit.create("로밍 지원", "5일 무료", BenefitType.SUBSCRIPTION)
                    )
            );

            //given
            given(userService.findCurrentSubscribedPlan(any())).willReturn(response1);

            // when
            ResultActions result = mockMvc.perform(get(USER_URL + "/plan")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document("get-my-plan-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("5G 프리미엄 요금제"));
        }

    }

    @Nested
    @DisplayName("유저 삭제 요청은")
    class DescribeDeleteUser {

        @BeforeEach
        public void initSecurity() {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("userId", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("성공적으로 삭제할 수 있다")
        void it_returns_delete_me() throws Exception {
            // given
            doNothing().when(userService)
                    .deleteUserById(any(String.class), any(HttpServletResponse.class));

            // when
            ResultActions result = mockMvc.perform(delete(USER_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document("delete-my-user-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isNoContent());
            verify(userService, times(1))
                    .deleteUserById(any(String.class), any(HttpServletResponse.class));
        }
    }

    @Nested
    @DisplayName("온보딩 요청은")
    class DescribeOnboarding {

        @BeforeEach
        public void initSecurity() {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("user123", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("성공적으로 온보딩 정보를 등록하고 200을 반환한다")
        void it_returns_onboarding_success() throws Exception {
            // given
            String json = """
                    {
                        "email": "user@example.com",
                        "planId": "plan123"
                    }
                    """;

            // when
            ResultActions result = mockMvc.perform(post(USER_URL + "/onboarding")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andDo(document("post-onboarding-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk());
            verify(userService).updateOnboardingInfo("user123", "user@example.com", "plan123");
        }
    }

    @Nested
    @DisplayName("유저 정보 조회 요청은")
    class DescribeGetMyInfo {

        @BeforeEach
        public void initSecurity() {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("user123", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("성공적으로 유저 정보를 조회하고 200을 반환한다")
        void it_returns_user_info_success() throws Exception {

            LocalDate createDate = LocalDate.now();
            // given
            ShowMyInfoResponse response = new ShowMyInfoResponse(
                    "user123",
                    "user@example.com",
                    "plan123",
                    UserRole.ROLE_USER,
                    createDate);
            given(userService.findMyInfoByUserId("user123")).willReturn(response);

            // when
            ResultActions result = mockMvc.perform(get(USER_URL + "/info")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document("get-user-info-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user123"))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.planId").value("plan123"));
        }
    }
}
