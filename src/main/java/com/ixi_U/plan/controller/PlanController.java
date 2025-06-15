package com.ixi_U.plan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.PlanDetailResponse;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/details/{planId}")
    public ResponseEntity<PlanDetailResponse> getPlanDetail(@PathVariable String planId) {

        PlanDetailResponse response = planService.findPlanDetail(planId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<PlanEmbeddedResponse> savePlan(@RequestBody SavePlanRequest request) throws JsonProcessingException {

        PlanEmbeddedResponse planEmbeddedResponse = planService.savePlan(request);

        return ResponseEntity.ok(planEmbeddedResponse);
    }
}
