package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

    @Transactional
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(UserException.USER_NOT_FOUND);
        }

        subscribedRepository.deleteAllByUserId(userId);

        userRepository.deleteById(userId);
    }
}
