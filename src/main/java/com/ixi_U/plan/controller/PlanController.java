package com.ixi_U.plan.controller;

import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
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

        GetPlansRequest.validate(size);
        Pageable pageable = PageRequest.ofSize(size);

        GetPlansRequest request = GetPlansRequest.of(planTypeStr, planSortOptionStr,
                searchKeyword, planId, cursorSortValue);
        SortedPlanResponse response = planService.findPlans(pageable, request);

        return ResponseEntity.ok(response);
    }
}
