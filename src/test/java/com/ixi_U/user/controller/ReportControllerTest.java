package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.common.config.SecurityConfig;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.dto.request.CreateReportRequest;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.ReportService;
import java.time.LocalDateTime;
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
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
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

@WebMvcTest(value = {ReportController.class})
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
public class ReportControllerTest {

    private static final String REPORT_URL = "/api/reports";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserRepository userRepository;

    @MockBean
    CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("신고 생성 요청은")
    class Describe_createReport {

        @BeforeEach
        void initSecurity() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("userId", null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
            );
        }

        @Test
        @DisplayName("정상적인 요청일 경우 201을 반환한다")
        void it_returns_201() throws Exception {
            // given
            doNothing().when(reportService).createReport(anyString(), any(Long.class));
            CreateReportRequest request = CreateReportRequest.from( 5L);

            // when
            ResultActions result = mockMvc.perform(post("/api/reports")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andDo(document("create-report-success"))
                            .andDo(print());

            // then
            result.andExpect(status().isCreated());
            verify(reportService, times(1)).createReport("userId", 5L);
        }
    }

    @Nested
    @DisplayName("신고 목록 조회 요청은")
    class Describe_showReports {

        @BeforeEach
        void initSecurity() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("userId", null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
            );
        }

        @Test
        @DisplayName("신고 목록을 반환한다")
        void it_returns_report_list() throws Exception {
            // given
            ShowReviewListResponse response = ShowReviewListResponse.of(List.of(ShowReviewResponse.of(1L, "진우",3,"comment"
            , LocalDateTime.of(2023,3,1,3,3,3))), false);
            when(reportService.showReport(any())).thenReturn(response);

            // when
            ResultActions result = mockMvc.perform(get(REPORT_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document("get-report-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.hasNextPage").value(false))
                            .andExpect(jsonPath("$.reviewResponseList[0].comment").value("comment"));
            verify(reportService, times(1)).showReport(any());
        }
    }
}
