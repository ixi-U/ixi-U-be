package com.ixi_U.user.service;

import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<SubscribedResponse> getMySubscribedPlans() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal instanceof String
                        ? (String) principal
                        : String.valueOf(principal);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return user.getSubscribedHistory().stream()
                .map(subscribed -> SubscribedResponse.builder()
                        .planName(subscribed.getPlan().getName())
                        .planState(String.valueOf(subscribed.getPlan().getState()))
                        .build())
                .collect(Collectors.toList());
    }
}
