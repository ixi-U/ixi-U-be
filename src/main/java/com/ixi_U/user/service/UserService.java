package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
//import com.ixi_U.user.dto.response.SubscribedResponse;
import com.ixi_U.user.dto.response.ShowCurrentSubscribedResponse;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.plan.entity.Plan;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubscribedRepository subscribedRepository;
    private final PlanRepository planRepository;

    public UserService(UserRepository userRepository, SubscribedRepository subscribedRepository,  PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.subscribedRepository = subscribedRepository;
        this.planRepository = planRepository;
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

//    // 사용자가 온보딩 화면에서 입력한 요금제로 업데이트
//    @Transactional
//    public void updateOnboardingInfo(String userId, String email, String planName) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
//
//        // 1. 이메일 업데이트 (withEmail 필요 시 생성자에 추가)
//        User updatedUser = user.withEmail(email);
//        userRepository.save(updatedUser);
//
//        // 2. 유저가 선택한 Plan 정보 조회
//        Plan plan = planRepository.findByName(planName)
//                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));
//
//        // 3. SUBSCRIBED 관계 생성
//        Subscribed subscribed = Subscribed.of(plan);
//        user.addSubscribed(subscribed);
//
//        userRepository.save(user);
//
//        subscribedRepository.save(subscribed);

    @Transactional
    public void updateOnboardingInfo(String userId, String email, String planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        User updatedUser = user.withEmail(email);
        userRepository.save(updatedUser);

        // 요금제 선택은 온보딩에서 필수 사항이 아님
        if (planId != null && !planId.isBlank()) {
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

            Subscribed subscribed = Subscribed.of(plan);
            updatedUser.addSubscribed(subscribed);
            subscribedRepository.save(subscribed);
        }
        userRepository.save(updatedUser);
    }
}
