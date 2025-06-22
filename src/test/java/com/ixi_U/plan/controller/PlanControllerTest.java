package com.ixi_U.plan.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanListDto;
import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.dto.PlansCountDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.BundledBenefitResponse;
import com.ixi_U.plan.dto.response.PlanAdminResponse;
import com.ixi_U.plan.dto.response.PlanDetailResponse;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import com.ixi_U.plan.dto.response.PlansCountResponse;
import com.ixi_U.plan.dto.response.SingleBenefitResponse;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.service.PlanService;
import com.ixi_U.util.constants.TestConstants;
import jakarta.servlet.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@ActiveProfiles("test")
@WebMvcTest(PlanController.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class PlanControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PlanService planService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("요금제 이름 목록 조회")
    class GetPlanNamesTest {

        @Test
        @DisplayName("요금제 이름 목록을 조회할 수 있다")
        void getPlanNames() throws Exception {
            // given
            PlanNameDto planA = new PlanNameDto("plan-A", "A 요금제");
            PlanNameDto planB = new PlanNameDto("plan-B", "B 요금제");
            given(planService.getPlanNameList()).willReturn(List.of(planA, planB));

            // when, then
            mockMvc.perform(get("/plans/summaries")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("plan-A"))
                    .andExpect(jsonPath("$[0].name").value("A 요금제"))
                    .andExpect(jsonPath("$[1].id").value("plan-B"))
                    .andExpect(jsonPath("$[1].name").value("B 요금제"))
                    .andDo(document("plans-get-names"));
        }
    }

    @Nested
    class SavePlanTest {

        @Test
        @DisplayName("요금제를 저장할 수 있다")
        public void savePlan() throws Exception {

            //given
            SavePlanRequest request = TestConstants.createSavePlanRequest();
            String description = TestConstants.createDescription();
            List<String> bundledBenefitNames = TestConstants.createBundledBenefitNames();
            List<String> singleBenefitNames = TestConstants.createSingleBenefitNames();
            List<String> singleBenefitTypes = TestConstants.createSingleBenefitTypes();

            Map<String, Object> metaData = Map.of(
                    "id", TestConstants.createPlanId(),
                    "name", request.name(),
                    "mobileDataLimitMb", request.mobileDataLimitMb(),
                    "monthlyPrice", request.monthlyPrice(),
                    "bundledBenefitNames", bundledBenefitNames,
                    "singleBenefitNames", singleBenefitNames,
                    "singleBenefitTypes", singleBenefitTypes
            );

            given(planService.savePlan(any(SavePlanRequest.class)))
                    .willReturn(PlanEmbeddedResponse.create(description, metaData));

            //when & then
            mockMvc.perform(post("/admin/plans/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value(description))
                    .andExpect(jsonPath("$.metaData.id").value(TestConstants.createPlanId()))
                    .andExpect(jsonPath("$.metaData.name").value(request.name()))
                    .andExpect(jsonPath("$.metaData.mobileDataLimitMb").value(
                            request.mobileDataLimitMb()))
                    .andExpect(jsonPath("$.metaData.monthlyPrice").value(request.monthlyPrice()))
                    .andExpect(jsonPath("$.metaData.bundledBenefitNames").isArray())
                    .andExpect(jsonPath("$.metaData.bundledBenefitNames",
                            hasSize(bundledBenefitNames.size())))
                    .andExpect(jsonPath("$.metaData.singleBenefitNames").isArray())
                    .andExpect(jsonPath("$.metaData.singleBenefitNames",
                            hasSize(singleBenefitNames.size())))
                    .andExpect(jsonPath("$.metaData.singleBenefitTypes").isArray())
                    .andExpect(jsonPath("$.metaData.singleBenefitTypes",
                            hasSize(singleBenefitTypes.size())))
                    .andDo(document("save-plans-success"));
        }
    }

    @Nested
    class GetPlansTest {

        @Test
        @DisplayName("요금제 목록을 정렬 조건으로 조회할 수 있다")
        void getPlansBySort() throws Exception {

            // given
            PlanListDto dto1 = new PlanListDto("1", "요금제1", "10GB", "2000", "300분", "200건", 29000,
                    5,
                    List.of(), List.of());
            PlanListDto dto2 = new PlanListDto("2", "요금제2", "무제한", "2000", "기본제공", "기본제공", 19000, 3,
                    List.of(), List.of());

            SortedPlanResponse response = new SortedPlanResponse(List.of(dto1, dto2), true, "2", 3);

            given(planService.findPlans(PageRequest.ofSize(2), GetPlansRequest.of(
                    "ONLINE", "PRIORITY", null, null, null)))
                    .willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans[0].id").value("1"))
                    .andExpect(jsonPath("$.plans[0].mobileDataLimitMb").value("10GB"))
                    .andExpect(jsonPath("$.plans[0].callLimitMinutes").value("300분"))
                    .andExpect(jsonPath("$.plans[1].id").value("2"))
                    .andExpect(jsonPath("$.plans[1].mobileDataLimitMb").value("무제한"))
                    .andExpect(jsonPath("$.plans[1].callLimitMinutes").value("기본제공"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3))
                    .andDo(document("get-plans-sort-success"))
                    .andDo(print());
        }

        @Test
        @DisplayName("요금제 목록을 검색으로 조회할 수 있다")
        void getPlansBySearch() throws Exception {

            // given
            PlanListDto dto2 = new PlanListDto("2", "요금제2", "8GB", "1000", "200분", "100건", 19000, 3,
                    List.of(), List.of());

            SortedPlanResponse response = new SortedPlanResponse(List.of(dto2), true, "2", 3);

            given(planService.findPlans(PageRequest.ofSize(2), GetPlansRequest.of(
                    "ONLINE", "PRIORITY", "제2", null, null)))
                    .willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            .param("searchKeyword", "제2")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans[0].id").value("2"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3))
                    .andDo(document("get-plans-search-success"))
                    .andDo(print());
        }

        @Test
        @DisplayName("유효하지 않은 정렬 조건일 경우 실패한다")
        void failIfInvalidSortOption() throws Exception {

            // given
            given(planService.findPlans(PageRequest.ofSize(2), GetPlansRequest.of(
                    "ONLINE", "PRIORIT", null, null, null)))
                    .willThrow(new GeneralException(PlanException.INVALID_SORT_VALUE));

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORIT")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andDo(document("get-plans-error-invalid-sort"));
        }

        @Test
        @DisplayName("유효하지 않은 요금제 타입일 경우 실패한다")
        void failIfInvalidPlanType() throws Exception {

            // given
            given(planService.findPlans(PageRequest.ofSize(2), GetPlansRequest.of(
                    "ONLINEE", "PRIORITY", null, null, null)))
                    .willThrow(new GeneralException(PlanException.INVALID_PLAN_TYPE));

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINEE")
                            .param("planSortOptionStr", "PRIORITY")
                            .with(user("tester").roles("USER")))
                    .andExpect(status().isBadRequest())
                    .andDo(document("get-plans-error-invalid-plan-type"));
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
                    .andDo(document("get-plans-error-invalid-size-parameter"));
        }
    }

    @DisplayName("요금제 상세 내역 조회 시 ")
    @Nested
    class GetPlanDetailTest {

        @DisplayName("성공한다.")
        @Test
        public void success() throws Exception {
            // given
            String name = "혜택 이름";
            String description = "혜택 설명";

            SingleBenefitResponse singleBenefit = new SingleBenefitResponse(name, description,
                    BenefitType.DEVICE.getType());
            List<SingleBenefitResponse> singleBenefits = IntStream.range(0, 3)
                    .mapToObj(i -> singleBenefit)
                    .toList();

            BundledBenefitResponse bundledBenefit = new BundledBenefitResponse(name,
                    description, 1, singleBenefits);
            List<BundledBenefitResponse> bundledBenefits = IntStream.range(0, 3)
                    .mapToObj(i -> bundledBenefit)
                    .toList();

            PlanDetailResponse response = new PlanDetailResponse(name, 100, 100,
                    100, 100, 30000,
                    PlanType.ONLINE.getPlanType(), "이용 시 유의사항", 100,
                    20, 40, 10.0,
                    "기타사항", bundledBenefits, singleBenefits);

            String planId = UUID.randomUUID().toString();
            given(planService.findPlanDetail(planId)).willReturn(response);

            // when, then
            mockMvc.perform(get("/plans/details/" + planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.mobileDataLimitMb").value(response.mobileDataLimitMb()))
                    .andExpect(jsonPath("$.sharedMobileDataLimitMb")
                            .value(response.sharedMobileDataLimitMb()))
                    .andExpect(jsonPath("$.callLimitMinutes").value(response.callLimitMinutes()))
                    .andExpect(jsonPath("$.messageLimit").value(response.messageLimit()))
                    .andExpect(jsonPath("$.monthlyPrice").value(response.monthlyPrice()))
                    .andExpect(jsonPath("$.planType").value(response.planType()))
                    .andExpect(jsonPath("$.usageCautions").value(response.usageCautions()))
                    .andExpect(jsonPath("$.mobileDataThrottleSpeedKbps")
                            .value(response.mobileDataThrottleSpeedKbps()))
                    .andExpect(jsonPath("$.minAge").value(response.minAge()))
                    .andExpect(jsonPath("$.maxAge").value(response.maxAge()))
                    .andExpect(jsonPath("$.pricePerKb").value(response.pricePerKb()))
                    .andExpect(jsonPath("$.etcInfo").value(response.etcInfo()))
                    .andExpect(jsonPath("$.bundledBenefits.length()")
                            .value(bundledBenefits.size()))
                    .andExpect(jsonPath("$.bundledBenefits[0].name")
                            .value(bundledBenefit.name()))
                    .andExpect(jsonPath("$.bundledBenefits[0].description")
                            .value(bundledBenefit.description()))
                    .andExpect(jsonPath("$.bundledBenefits[0].singleBenefits.length()")
                            .value(singleBenefits.size()))
                    .andExpect(jsonPath("$.singleBenefits.length()").value(singleBenefits.size()))
                    .andExpect(jsonPath("$.singleBenefits[0].name").value(singleBenefit.name()))
                    .andExpect(jsonPath("$.singleBenefits[0].description")
                            .value(singleBenefit.description()))
                    .andExpect(jsonPath("$.singleBenefits[0].benefitType")
                            .value(singleBenefit.benefitType()))
                    .andDo(document("get-plans-details-success"));
        }
    }

    @Nested
    @DisplayName("어드민 요금제 전체 조회 시")
    class GetPlansForAdmin {

        @Test
        @DisplayName("전체 요금제를 조회하고 200을 반환한다")
        void shouldReturnPlans() throws Exception {
            when(planService.getPlansForAdmin()).thenReturn(
                    List.of(new PlanAdminResponse("id", "요금제A", PlanState.ABLE, "")));

            mockMvc.perform(get("/admin/plans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("id"));
        }
    }

    @Nested
    @DisplayName("어드민 요금제 상태 토글 시")
    class TogglePlanState {

        @Test
        @DisplayName("요금제 상태를 토글하고 200을 반환한다")
        void shouldToggleState() throws Exception {
            doNothing().when(planService).togglePlanState("id");

            mockMvc.perform(patch("/admin/plans/id/toggle"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("어드민 요금제 비활성화 시")
    class DisablePlan {

        @Test
        @DisplayName("요금제를 비활성화하고 200을 반환한다")
        void shouldDisablePlan() throws Exception {
            doNothing().when(planService).disablePlan("id");

            mockMvc.perform(patch("/admin/plans/id/disable"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("요금제 요약 조회 시")
    class GetPlanNames {

        @Test
        @DisplayName("요금제 요약 목록을 조회하고 200을 반환한다")
        void shouldReturnPlanNames() throws Exception {
            when(planService.getPlanNameList()).thenReturn(
                    Collections.singletonList(new PlanNameDto("id", "요금제A")));

            mockMvc.perform(get("/plans/summaries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("id"))
                    .andExpect(jsonPath("$[0].name").value("요금제A"));
        }
    }

    @Nested
    @DisplayName("요금제 임베드 수행 시")
    class EmbedPlan {

        @Test
        @DisplayName("전체 요금제를 임베드하고 200을 반환한다")
        void shouldEmbedAllPlan() throws Exception {
            doNothing().when(planService).embedAllPlan();

            mockMvc.perform(get("/plans/embed"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("요금제 개수 조회 시")
    class CountPlans {

        @Test
        @DisplayName("성공한다")
        void success() throws Exception {
            // 1) given
            PlansCountResponse stub =
                    PlansCountResponse.from(new PlansCountDto(500, 100, 100, 100, 100));

            given(planService.countPlans()).willReturn(stub);

            // 2) when & then
            mockMvc.perform(get("/plans/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.all").value(500))
                    .andExpect(jsonPath("$.fiveGLte").value(100))
                    .andExpect(jsonPath("$.online").value(100))
                    .andExpect(jsonPath("$.tabletSmartwatch").value(100))
                    .andExpect(jsonPath("$.dualNumber").value(100))
                    .andDo(document("get-plans-count-success",
                            responseFields(
                                    fieldWithPath("all").description("전체 요금제 개수"),
                                    fieldWithPath("fiveGLte").description("5G/LTE 요금제 개수"),
                                    fieldWithPath("online").description("온라인 전용 요금제 개수"),
                                    fieldWithPath("tabletSmartwatch").description(
                                            "태블릿·스마트워치 요금제 개수"),
                                    fieldWithPath("dualNumber").description("듀얼넘버 요금제 개수")
                            )
                    ));
        }
    }
}
