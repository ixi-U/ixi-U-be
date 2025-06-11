package com.ixi_U.plan.controller;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<SortedPlanResponse> getPlans(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String planTypeStr,
            @RequestParam(defaultValue = "PRIORITY") String planSortOptionStr,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) Integer cursorSortValue) {

        if (size < 1) {
            throw new GeneralException(PlanException.INVALID_PARAMETER);
        }

        Pageable pageable = PageRequest.ofSize(size);
        SortedPlanResponse response = planService.findPlans(pageable, planTypeStr,
                planSortOptionStr, searchKeyword, planId, cursorSortValue);

        return ResponseEntity.ok(response);
    }
}
