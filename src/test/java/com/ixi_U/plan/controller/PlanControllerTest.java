package com.ixi_U.plan.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.service.PlanService;
import jakarta.servlet.Filter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(PlanController.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class PlanControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PlanService planService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        // UTF-8 필터(옵션)
        Filter encodingFilter = new CharacterEncodingFilter("utf-8", true);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(encodingFilter)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Nested
    class GetPlans {

        @Test
        @DisplayName("요금제 목록을 정렬 조건으로 조회할 수 있다")
        void getPlansBySort() throws Exception {

            // given
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000, 300, 200, 29000, 5,
                    List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of());
            SortedPlanResponse response =
                    new SortedPlanResponse(new SliceImpl<>(List.of(dto1, dto2)), "2", 3);

            given(planService.findPlans(PageRequest.ofSize(2),
                    "ONLINE", "PRIORITY", null, null, null)).willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            // 보안필터를 통과시키려면 필요시 Mock 사용자 주입
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans.content[0].id").value("1"))
                    .andExpect(jsonPath("$.plans.content[1].id").value("2"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3))
                    .andDo(document("plans-get-sort"))
                    .andDo(print());
        }

        @Test
        @DisplayName("요금제 목록을 검색으로 조회할 수 있다")
        void getPlansBySearch() throws Exception {

            // given
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of());
            SortedPlanResponse response =
                    new SortedPlanResponse(new SliceImpl<>(List.of(dto2)), "2", 3);

            given(planService.findPlans(PageRequest.ofSize(2),
                    "ONLINE", "PRIORITY", "제2", null, null)).willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            .param("searchKeyword", "제2")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans.content[0].id").value("2"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3))
                    .andDo(document("plans-get-search"))
                    .andDo(print());
        }

        @Test
        @DisplayName("유효하지 않은 정렬 조건일 경우 실패한다")
        void failIfInvalidSortOption() throws Exception {

            // given
            given(planService.findPlans(PageRequest.ofSize(2),
                    "ONLINE", "PRIORIT", null, null, null))
                    .willThrow(new GeneralException(PlanException.INVALID_SORT_VALUE));

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORIT")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andDo(document("plans-get-invalid-sort"));
        }

        @Test
        @DisplayName("유효하지 않은 요금제 타입일 경우 실패한다")
        void failIfInvalidPlanType() throws Exception {

            // given
            given(planService.findPlans(PageRequest.ofSize(2),
                    "ONLINEE", "PRIORITY", null, null, null))
                    .willThrow(new GeneralException(PlanException.INVALID_PLAN_TYPE));

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINEE")
                            .param("planSortOptionStr", "PRIORITY")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andDo(document("plans-get-invalid-plan-type"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("파라미터 size가 1보다 작을 경우 실패한다")
        void failIfSizeLessThanOne(int size) throws Exception {

            // given, when, then
            mockMvc.perform(get("/plans")
                            .param("size", String.valueOf(size))
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andDo(document("plans-get-invalid-size-parameter"));
        }
    }
}