package com.ixi_U.plan.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.service.PlanService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlanController.class)
@ExtendWith(SpringExtension.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanService planService;

    @Nested
    class getPlans {

        @Test
        @DisplayName("요금제 목록을 정렬 조건으로 조회할 수 있다")
        void getPlansBySort() throws Exception {

            // given
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000, 300, 200, 29000, 5,
                    List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of());
            Slice<PlanSummaryDto> slice = new SliceImpl<>(List.of(dto1, dto2));
            SortedPlanResponse response = new SortedPlanResponse(slice, "2", 3);

            given(planService.findPlans(
                    PageRequest.ofSize(2), "ONLINE", "PRIORITY", null, null, null))
                    .willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans.content[0].id").value("1"))
                    .andExpect(jsonPath("$.plans.content[1].id").value("2"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3));
        }

        @Test
        @DisplayName("요금제 목록을 검색으로 조회할 수 있다")
        void getPlansBySearch() throws Exception {

            // given
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000, 300, 200, 29000, 5,
                    List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of());
            Slice<PlanSummaryDto> slice = new SliceImpl<>(List.of(dto2));
            SortedPlanResponse response = new SortedPlanResponse(slice, "2", 3);

            given(planService.findPlans(
                    PageRequest.ofSize(2), "ONLINE", "PRIORITY", "제2", null, null))
                    .willReturn(response);

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORITY")
                            .param("searchKeyword", "제2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plans.content[0].id").value("2"))
                    .andExpect(jsonPath("$.lastPlanId").value("2"))
                    .andExpect(jsonPath("$.lastSortValue").value(3));
        }

        @Test
        @DisplayName("유효하지 않은 정렬 조건일 경우 실패한다")
        void failIfInvalidSortOption() throws Exception {

            // given
            PlanSummaryDto dto1 = new PlanSummaryDto("1", "요금제1", 10000, 2000, 300, 200, 29000, 5,
                    List.of());
            PlanSummaryDto dto2 = new PlanSummaryDto("2", "요금제2", 8000, 1000, 200, 100, 19000, 3,
                    List.of());

            given(planService.findPlans(
                    PageRequest.ofSize(2), "ONLINE", "PRIORIT", null, null, null))
                    .willThrow(new GeneralException(PlanException.INVALID_SORT_VALUE));

            // when, then
            mockMvc.perform(get("/plans")
                            .param("size", "2")
                            .param("planTypeStr", "ONLINE")
                            .param("planSortOptionStr", "PRIORIT"))
                    .andExpect(status().isBadRequest());
        }
    }

}