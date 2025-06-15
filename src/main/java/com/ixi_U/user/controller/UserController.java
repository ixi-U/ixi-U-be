package com.ixi_U.user.controller;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.dto.response.PlanResponse;
import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal CustomOAuth2User user) {
        userService.deleteUserById(user.getUserId());
        return ResponseEntity.noContent().build();
    }

}