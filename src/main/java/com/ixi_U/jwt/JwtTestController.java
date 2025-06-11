// JwtTestController.java
package com.ixi_U.jwt;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/generate-token")
    public ResponseEntity<String> generateToken(@RequestParam Long kakaoId, HttpServletResponse response) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = jwtTokenProvider.generateToken(user.getId(), "ROLE_USER");

        // 쿠키에 넣어주기
        Cookie cookie = new Cookie("access_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        return ResponseEntity.ok("JWT 발급 완료: " + jwt);
    }
}
