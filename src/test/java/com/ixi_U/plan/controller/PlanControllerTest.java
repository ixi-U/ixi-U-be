package com.ixi_U.plan.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.BundledBenefitResponse;
import com.ixi_U.plan.dto.response.PlanDetailResponse;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import com.ixi_U.plan.dto.response.SingleBenefitResponse;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.service.PlanService;
import com.ixi_U.util.constants.TestConstants;
import jakarta.servlet.Filter;
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
import org.springframework.data.domain.SliceImpl;
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

            System.out.println("bundledBenefitNames = " + bundledBenefitNames);
            System.out.println("singleBenefitTypes = " + singleBenefitTypes);
            System.out.println("singleBenefitNames = " + singleBenefitNames);

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
            mockMvc.perform(post("/plans/save")
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
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000, 300, 200, 29000, 5,
                    List.of(), List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of(), List.of());
            SortedPlanResponse response =
                    new SortedPlanResponse(new SliceImpl<>(List.of(dto1, dto2)), "2", 3);

            given(planService.findPlans(PageRequest.ofSize(2), GetPlansRequest.of(
                    "ONLINE", "PRIORITY", null, null, null)))
                    .willReturn(response);

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
                    .andDo(document("get-plans-sort-success"))
                    .andDo(print());
        }

        @Test
        @DisplayName("요금제 목록을 검색으로 조회할 수 있다")
        void getPlansBySearch() throws Exception {

            // given
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of(), List.of());
            SortedPlanResponse response =
                    new SortedPlanResponse(new SliceImpl<>(List.of(dto2)), "2", 3);

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
                    .andExpect(jsonPath("$.plans.content[0].id").value("2"))
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
                    20, 40, 10,
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
}
