package com.ixi_U.plan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    PlanRepository planRepository;

    @InjectMocks
    PlanService planService;

    @Nested
    class findPlansTest {

        @ParameterizedTest
        @CsvSource({
                "5G/LTE, PRIORITY",
                "ONLINE, PRICE_ASC",
                "TABLET/SMARTWATCH, PRICE_DESC",
                "DUAL_NUMBER, DATA_DESC"
        })
        @DisplayName("정렬 조건으로 요금제를 조회할 수 있다")
        void searchBySort(String planTypeStr, String planSortOptionStr) {
            // given
            PageRequest pageable = PageRequest.ofSize(3);

            PlanSummaryDto dto1 = new PlanSummaryDto("plan-A", "A 요금제", 5000, 1000, 200, 300, 29000, 1, List.of(), List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("plan-B", "B 요금제", 3000, 500, 100, 200, 19000, 2, List.of(), List.of());
            PlanSummaryDto dto3 = new PlanSummaryDto("plan-C", "C 요금제", 1000, 300, 50, 100, 9900, 3, List.of(), List.of());

            Slice<PlanSummaryDto> slice = new SliceImpl<>(List.of(dto1, dto2, dto3), pageable, true);

            given(planRepository.findPlans(pageable, PlanType.from(planTypeStr),
                    PlanSortOption.from(planSortOptionStr), null, null, null))
                    .willReturn(slice);

            // when
            SortedPlanResponse result = planService.findPlans(
                    pageable, GetPlansRequest.of(planTypeStr, planSortOptionStr, null, null, null)
            );

            // then
            assertThat(result.plans().getContent()).containsExactly(dto1, dto2, dto3);
            verify(planRepository).findPlans(pageable, PlanType.from(planTypeStr),
                    PlanSortOption.from(planSortOptionStr), null, null, null
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"ONLINEE", " ", ""})
        @DisplayName("요금제 타입이 유효하지 않을 때 실패한다")
        void searchFailWhenPlanTypeIsInvalid(String planType) {

            // given, when, then
            assertThatThrownBy(() ->
                    planService.findPlans(
                            PageRequest.ofSize(3),
                            GetPlansRequest.of(planType, "PRIORITY", null, null, null))
            )
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining(PlanException.INVALID_PLAN_TYPE.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"PRIORITYY", " ", ""})
        @DisplayName("정렬 조건이 유효하지 않을 때 실패한다")
        void searchFailWhenSortOptionIsInvalid(String planSortOptionStr) {

            // given, when, then
            assertThatThrownBy(() ->
                    planService.findPlans(
                            PageRequest.ofSize(3),
                            GetPlansRequest.of("ONLINE", planSortOptionStr, null, null, null))
            )
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining(PlanException.INVALID_SORT_VALUE.getMessage());
        }
    }
}
