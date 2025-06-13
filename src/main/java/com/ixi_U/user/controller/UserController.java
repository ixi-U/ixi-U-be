package com.ixi_U.user.controller;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.user.dto.response.PlanResponse;
import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}