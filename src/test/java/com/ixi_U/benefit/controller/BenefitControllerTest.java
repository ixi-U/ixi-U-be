package com.ixi_U.benefit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.benefit.dto.request.SaveBundledBenefitRequest;
import com.ixi_U.benefit.dto.request.SaveSingleBenefitRequest;
import com.ixi_U.benefit.dto.response.FindBundledBenefitResponse;
import com.ixi_U.benefit.dto.response.FindSingleBenefitResponse;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.benefit.service.BenefitService;

import java.util.List;

import com.ixi_U.security.jwt.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(value = {BenefitController.class})
@ExtendWith({RestDocumentationExtension.class})
@ActiveProfiles("test")
class BenefitControllerTest {

    private static final String BENEFIT_URL = "/api/benefits";

    private MockMvc mockMvc;

    @MockBean
    BenefitService benefitService;

    @MockBean
    JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("혜택 목록 조회 요청은")
    class DescribeBenefitList {

        List<FindSingleBenefitResponse>  findSingleBenefitResponses;
        List<FindBundledBenefitResponse> findBundledBenefitResponses;

        @BeforeEach
        public void init(){
            FindSingleBenefitResponse single1 = FindSingleBenefitResponse.create(
                    "s1", "스마트폰 제공", "최신형 기기", BenefitType.DEVICE);

            FindSingleBenefitResponse single2 = FindSingleBenefitResponse.create(
                    "s2", "요금 20% 할인", "12개월 약정 시", BenefitType.DISCOUNT);

            FindSingleBenefitResponse single3 = FindSingleBenefitResponse.create(
                    "s3", "왓챠 6개월", "모든 콘텐츠 무제한", BenefitType.SUBSCRIPTION);

            FindSingleBenefitResponse single4 = FindSingleBenefitResponse.create(
                    "s4", "태블릿 제공", "LTE 모델", BenefitType.DEVICE);

            findSingleBenefitResponses = List.of(single1,single2,single3,single4);

            FindBundledBenefitResponse bundled1 = FindBundledBenefitResponse.create(
                    "b1", "기기+할인 패키지", "스마트폰과 요금 할인", 2, List.of(single1, single2));

            FindBundledBenefitResponse bundled2 = FindBundledBenefitResponse.create(
                    "b2", "OTT 패키지", "왓챠 구독 포함", 1, List.of(single3));

            FindBundledBenefitResponse bundled3 = FindBundledBenefitResponse.create(
                    "b3", "기기 패키지", "여러 기기 제공", 2, List.of(single1, single4));

            FindBundledBenefitResponse bundled4 = FindBundledBenefitResponse.create(
                    "b4", "풀 패키지", "모든 혜택 포함", 3, List.of(single1, single2, single3, single4));

            findBundledBenefitResponses = List.of(bundled1,bundled2,bundled3,bundled4);
        }

        @Test
        @DisplayName("묶음 혜택 응답 리스트를 잘 반환한다.")
        void it_returns_bundeled_list() throws Exception {
            // given

            given(benefitService.findAllBundledBenefit()).willReturn(findBundledBenefitResponses);

            // when
            ResultActions result = mockMvc.perform(
                            get(BENEFIT_URL+"/bundled")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document(
                            "get-bundled-benefit-list-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(findBundledBenefitResponses.size()))
                    .andExpect(jsonPath("$[0].id").value("b1"));


        }

        @Test
        @DisplayName("단일 혜택 응답 리스트를 잘 반환한다.")
        void it_returns_singled_list() throws Exception {
            // given

            given(benefitService.findAllSingleBenefit()).willReturn(findSingleBenefitResponses);

            // when
            ResultActions result = mockMvc.perform(
                            get(BENEFIT_URL+"/single")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document(
                            "get-single-benefit-list-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(findSingleBenefitResponses.size()))
                    .andExpect(jsonPath("$[0].id").value("s1"));

        }
    }

    @Nested
    @DisplayName("혜택 저장 요청은")
    class DescribeBenefitSave {

        @Test
        @DisplayName("묶음 혜택 저장 요청을 잘 수행한다.")
        void it_returns_save_bundled() throws Exception {

            // given
            SaveBundledBenefitRequest saveBundledBenefitRequest = SaveBundledBenefitRequest.create(
                    "묶음 상품","묶음 상품 설명",1,List.of(String.valueOf(1),String.valueOf(2),String.valueOf(3))
            );
            doNothing().when(benefitService).saveBundledBenefit(saveBundledBenefitRequest);

            // when
            ResultActions result = mockMvc.perform(
                            post(BENEFIT_URL+"/bundled")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(saveBundledBenefitRequest)))
                                    .andDo(document("save-bundled-benefit-success"))
                                    .andDo(print());

            // then
            result.andExpect(status().isCreated());
            verify(benefitService, times(1)).saveBundledBenefit(any(SaveBundledBenefitRequest.class));
        }

        @Test
        @DisplayName("단일 혜택 저장 요청을 잘 수행한다.")
        void it_returns_save_single() throws Exception {

            // given
            SaveSingleBenefitRequest saveSingleBenefitRequest = SaveSingleBenefitRequest.create(
          "싱글요금제","subscript",BenefitType.SUBSCRIPTION
            );

            doNothing().when(benefitService).saveSingleBenefit(saveSingleBenefitRequest);

            // when
            ResultActions result = mockMvc.perform(
                            post(BENEFIT_URL+"/single")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(saveSingleBenefitRequest)))
                    .andDo(document("save-single-benefit-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isCreated());
            verify(benefitService, times(1)).saveSingleBenefit(any(SaveSingleBenefitRequest.class));
        }
    }
}




