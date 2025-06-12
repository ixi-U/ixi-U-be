package com.ixi_U.plan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ixi_U.common.exception.GeneralException;
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
                "DUAL_NUMBER, DATA_DESC"})
        @DisplayName("정렬 조건으로 요금제를 조회할 수 있다")
        void searchBySort(String planTypeStr, String planSortOptionStr) {

            // given
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000,
                    300, 200, 29000, 5, List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000,
                    200, 100, 19000, 3, List.of());
            PlanSummaryDto dto3 = new PlanSummaryDto("3", "요금제3", 20000, 3000,
                    500, 300, 49000, 1, List.of());

            PageRequest pageable = PageRequest.ofSize(3);
            Slice<PlanSummaryDto> slice =
                    new SliceImpl<>(List.of(dto1, dto2, dto3), pageable, true);

            given(planRepository.findPlans(pageable, PlanType.from(planTypeStr),
                    PlanSortOption.from(planSortOptionStr), null, null, null))
                    .willReturn(slice);

            // when
            SortedPlanResponse result = planService.findPlans(
                    pageable, GetPlansRequest.of(planTypeStr, planSortOptionStr, null, null, null));

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
