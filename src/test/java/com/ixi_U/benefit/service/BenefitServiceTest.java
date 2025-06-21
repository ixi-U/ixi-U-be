package com.ixi_U.benefit.service;

import com.ixi_U.benefit.dto.request.SaveBundledBenefitRequest;
import com.ixi_U.benefit.dto.request.SaveSingleBenefitRequest;
import com.ixi_U.benefit.dto.response.FindBundledBenefitResponse;
import com.ixi_U.benefit.dto.response.FindSingleBenefitResponse;
import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.benefit.repository.BundledBenefitRepository;
import com.ixi_U.benefit.repository.SingleBenefitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    @Mock
    private BundledBenefitRepository bundledBenefitRepository;

    @Mock
    private SingleBenefitRepository singleBenefitRepository;

    @InjectMocks
    private BenefitService benefitService;

//    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("모든 단일 혜택을 조회한다")
    void findAllSingleBenefit() {
        // given
        SingleBenefit benefit = SingleBenefit.create("넷플릭스", "3개월 무료", BenefitType.DEVICE);
        when(singleBenefitRepository.findAll()).thenReturn(List.of(benefit));

        // when
        List<FindSingleBenefitResponse> responses = benefitService.findAllSingleBenefit();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("넷플릭스");
    }

    @Test
    @DisplayName("모든 묶음 혜택을 조회한다")
    void findAllBundledBenefit() {
        // given
        SingleBenefit s1 = SingleBenefit.create("넷플릭스", "3개월", BenefitType.DEVICE);

        BundledBenefit b1 = BundledBenefit.create("엔터 패키지", "설명", 1);
        b1.addAllSingleBenefit(List.of(s1));

        when(bundledBenefitRepository.findAll()).thenReturn(List.of(b1));

        // when
        List<FindBundledBenefitResponse> result = benefitService.findAllBundledBenefit();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).singleBenefitResponses()).hasSize(1);
        assertThat(result.get(0).singleBenefitResponses().get(0).name()).isEqualTo("넷플릭스");
    }

    @Test
    @DisplayName("단일 혜택을 저장한다")
    void saveSingleBenefit() {
        // given
        SaveSingleBenefitRequest request = new SaveSingleBenefitRequest("왓챠", "프리미엄", BenefitType.DEVICE);

        // when
        benefitService.saveSingleBenefit(request);

        // then
        verify(singleBenefitRepository, times(1)).save(any(SingleBenefit.class));
    }

    @Test
    @DisplayName("묶음 혜택을 저장한다")
    void saveBundledBenefit() {
        // given
        String sId = "s1";
        SaveBundledBenefitRequest request = new SaveBundledBenefitRequest("통신 패키지", "통신혜택", 2, List.of(sId));

        SingleBenefit s = SingleBenefit.create("왓챠", "프리미엄", BenefitType.DEVICE);
        when(singleBenefitRepository.findAllById(List.of(sId))).thenReturn(List.of(s));

        // when
        benefitService.saveBundledBenefit(request);

        // then
        verify(bundledBenefitRepository, times(1)).save(any(BundledBenefit.class));
    }
}