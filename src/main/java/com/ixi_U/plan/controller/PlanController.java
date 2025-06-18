package com.ixi_U.plan.controller;

import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.PlanAdminResponse;
import com.ixi_U.plan.dto.response.PlanDetailResponse;
import com.ixi_U.plan.dto.response.PlanEmbeddedResponse;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping("/plans")
    public ResponseEntity<SortedPlanResponse> getPlans(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String planTypeStr,
            @RequestParam(defaultValue = "PRIORITY") String planSortOptionStr,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) Integer cursorSortValue){

        GetPlansRequest.validate(size);
        Pageable pageable = PageRequest.ofSize(size);

        GetPlansRequest request = GetPlansRequest.of(planTypeStr, planSortOptionStr,
                searchKeyword, planId, cursorSortValue);
        SortedPlanResponse response = planService.findPlans(pageable, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/details/{planId}")
    public ResponseEntity<PlanDetailResponse> getPlanDetail(@PathVariable String planId) {

        PlanDetailResponse response = planService.findPlanDetail(planId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/plans/save")
    public ResponseEntity<PlanEmbeddedResponse> savePlan(@RequestBody SavePlanRequest request) {
        log.info("요금제 등록 요청 : {}", request.name());

        PlanEmbeddedResponse planEmbeddedResponse = planService.savePlan(request);

        return ResponseEntity.ok(planEmbeddedResponse);
    }

    // 어드민 요금제 조회
    @GetMapping("/admin/plans")
    public ResponseEntity<List<PlanAdminResponse>> getPlansForAdmin() {
        return ResponseEntity.ok(planService.getPlansForAdmin());
    }

    @PatchMapping("/admin/plans/{planId}/toggle")
    public ResponseEntity<Void> togglePlanState(@PathVariable String planId) {
        planService.togglePlanState(planId);
        return ResponseEntity.ok().build();
    }

    // 요금제 상태 비가시화(비활성화)
    @PatchMapping("/admin/plans/{planId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disablePlan(@PathVariable String planId) {
        planService.disablePlan(planId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/embed")
    public ResponseEntity<Void> embedPlan() {

        planService.embedAllPlan();

        return ResponseEntity.status(200).build();
    }
}
