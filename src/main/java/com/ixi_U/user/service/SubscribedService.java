package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.PlanException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.CreateSubscribedRequest;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscribedService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    public SubscribedService(UserRepository userRepository, PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public void updateSubscribed(String userId, CreateSubscribedRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

        // 유저의 가장 최신 구독 가져오기
        Subscribed latestSubscribed = user.getSubscribedHistory().isEmpty()
                ? null
                : user.getSubscribedHistory().get(user.getSubscribedHistory().size() - 1);

        // 현재 요금제와 같은지 체크
        if (latestSubscribed != null && latestSubscribed.getPlan().equals(plan)) {
            throw new GeneralException(PlanException.ALREADY_SUBSCRIBED_PLAN);
        }

        user.addSubscribed(Subscribed.of(plan));

        userRepository.save(user);
    }
}