package com.ixi_U.user.service;

import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.auth.dto.CustomOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<SubscribedResponse> getMySubscribedPlans() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String userId;
        if (principal instanceof String) {
            userId = (String) principal;
        } else if (principal instanceof CustomOAuth2User) {
            userId = ((CustomOAuth2User) principal).getUserId();
        } else {
            throw new RuntimeException("알 수 없는 사용자 인증 정보입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return user.getSubscribedHistory().stream()
                .map(subscribed -> SubscribedResponse.builder()
                        .planName(subscribed.getPlan().getName())
                        .planState(String.valueOf(subscribed.getPlan().getPlanState()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public User changeName(String userId, String newName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        User updated = user.withName(newName);

        return userRepository.save(updated);
    }

    // UserService.java
    @Transactional
    public void removeRefreshToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        User updated = user.withRefreshToken(null); // null로 초기화 (or 빈 문자열 ..)
        userRepository.save(updated);
    }
}


