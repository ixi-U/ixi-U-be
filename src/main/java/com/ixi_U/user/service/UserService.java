package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubscribedRepository subscribedRepository;

    public UserService(UserRepository userRepository, SubscribedRepository subscribedRepository) {
        this.userRepository = userRepository;
        this.subscribedRepository = subscribedRepository;
    }

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

    @Transactional
    public User changeName(String userId, String newName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        // setter 없이 withName으로 새 객체 생성
        User updated = user.withName(newName);

        return userRepository.save(updated);
    }

    @Transactional
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(UserException.USER_NOT_FOUND);
        }

        subscribedRepository.deleteAllByUserId(userId);

        userRepository.deleteById(userId);
    }
}
