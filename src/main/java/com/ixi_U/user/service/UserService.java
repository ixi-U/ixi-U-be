package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.auth.dto.CustomOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

        String userId;
        if (principal instanceof String) {
            userId = (String) principal;
        } else if (principal instanceof CustomOAuth2User) {
            userId = ((CustomOAuth2User) principal).getUserId();
        } else {
            throw new RuntimeException("알 수 없는 사용자 인증 정보입니다.");
        }

    public ShowCurrentSubscribedResponse findCurrentSubscribedPlan(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Subscribed latestSubscribed = user.getSubscribedHistory().stream()
                .max(Comparator.comparing(Subscribed::getCreatedAt))
                .orElse(null);

        if (latestSubscribed == null) {
            return null;
        }

        var plan = latestSubscribed.getPlan();

        User updated = user.withName(newName);
      
        return ShowCurrentSubscribedResponse.of(
                plan.getName(),
                plan.getMobileDataLimitMb(),
                plan.getMonthlyPrice(),
                plan.getPricePerKb(),
                plan.getBundledBenefits(),
                plan.getSingleBenefits()
        );
    }

    @Transactional(readOnly = true)
    public ShowMyInfoResponse findMyInfoByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return ShowMyInfoResponse.of(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserRole(),
                LocalDate.parse(
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        );
    }

    // UserService.java
    @Transactional
    public void removeRefreshToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        User updated = user.withRefreshToken(null); // null로 초기화 (or 빈 문자열 ..)
        userRepository.save(updated);
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
