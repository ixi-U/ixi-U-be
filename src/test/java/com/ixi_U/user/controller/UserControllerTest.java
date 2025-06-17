package com.ixi_U.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.common.config.SecurityConfig;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.dto.response.PlanResponse;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.service.UserService;
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
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    JwtTokenProvider oAuth2SuccessHandler;

    @MockBean
    CustomOAuth2UserService customOAuth2UserService;

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
        public void initSecurity(){
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("userId", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
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
            ResultActions result = mockMvc.perform(get(USER_URL+"/plan")
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
        public void initSecurity(){
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("userId", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("성공적으로 삭제할 수 있다")
        void it_returns_delete_me() throws Exception {

            //given
            doNothing().when(userService).deleteUserById(any());

            //when
            ResultActions result = mockMvc.perform(delete(USER_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document("delete-my-user-success"))
                    .andDo(print());

            //then
            result.andExpect(status().isNoContent());
            verify(userService, times(1)).deleteUserById(any(String.class));

        }
    }



}
