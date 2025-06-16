package com.ixi_U.user.controller;

import com.ixi_U.user.dto.response.PlanResponse;
import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyPlan(Authentication authentication) {
        // 1. 인증 여부 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 2. 사용자 요금제 조회
            List<SubscribedResponse> subscribedList = userService.getMySubscribedPlans();

            // 3. 요금제 없음
            if (subscribedList.isEmpty()) {
                return ResponseEntity.ok("아직 등록된 요금제가 없습니다.");
            }

            // 4. 첫 번째 요금제 반환
            PlanResponse plan = subscribedList.get(0).plan();
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal String userId) {
        userService.deleteUserById(userId);

        return ResponseEntity.noContent().build();
    }
}

