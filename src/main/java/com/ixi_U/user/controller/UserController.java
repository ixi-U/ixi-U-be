package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.OnboardingRequest;
import com.ixi_U.user.dto.response.PlanResponse;
import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyPlan() {
        List<SubscribedResponse> subscribedList = userService.getMySubscribedPlans();

        if (subscribedList.isEmpty()) {
            return ResponseEntity.ok("아직 등록된 요금제가 없습니다.");

        }

        // 하나만 있다고 가정
        PlanResponse plan = subscribedList.get(0).plan();
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal String userId) {
        userService.deleteUserById(userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/onboarding")
    public ResponseEntity<Void> onboarding(@RequestBody OnboardingRequest request,
                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = (String) authentication.getPrincipal();
        userService.updateOnboardingInfo(userId, request.email(), request.selectedPlanName());
        return ResponseEntity.ok().build();
    }

}