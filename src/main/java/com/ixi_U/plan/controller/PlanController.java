package com.ixi_U.plan.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    @GetMapping("/auth-test")
    public ResponseEntity<String> testAuthentication(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("🔒 인증되지 않은 사용자입니다.");
        }

        // 인증된 사용자 ID 확인 (subject로 넣은 userId)
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok("✅ 인증 성공! 사용자 ID: " + userId);
    }
}