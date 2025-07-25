package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.OnboardingRequest;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/plan")
    public ResponseEntity<?> getMyPlan(@AuthenticationPrincipal String userId) {
        ShowCurrentSubscribedResponse subscribed = userService.findCurrentSubscribedPlan(userId);

        return ResponseEntity.ok(Objects.requireNonNullElse(subscribed, "아직 등록된 요금제가 없습니다."));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal String userId,
            HttpServletResponse response) {
        userService.deleteUserById(userId, response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/onboarding")
    public ResponseEntity<Void> onboarding(@RequestBody OnboardingRequest request,
            @AuthenticationPrincipal String userId) {
        log.info("[Onboarding] userId: {}", userId);
        userService.updateOnboardingInfo(userId, request.email(), request.planId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<ShowMyInfoResponse> getMyInfo(@AuthenticationPrincipal String userId) {
        ShowMyInfoResponse response = userService.findMyInfoByUserId(userId);

        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."
            );
        }
        return ResponseEntity.ok(response);
    }
}
