package com.ixi_U.user.controller;

import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.service.UserService;
import java.util.Objects;
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

    @GetMapping("/plan")
    public ResponseEntity<?> getMyPlan(@AuthenticationPrincipal String userId) {
        ShowCurrentSubscribedResponse subscribed = userService.findCurrentSubscribedPlan(userId);

        return ResponseEntity.ok(Objects.requireNonNullElse(subscribed, "아직 등록된 요금제가 없습니다."));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal String userId) {
        userService.deleteUserById(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/info")
    public ResponseEntity<ShowMyInfoResponse> getMyInfo(@AuthenticationPrincipal String userId) {
        ShowMyInfoResponse response = userService.findMyInfoByUserId(userId);

        return ResponseEntity.ok(response);
    }

}