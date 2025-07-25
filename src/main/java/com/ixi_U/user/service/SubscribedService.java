package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.PlanException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.dto.response.ShowSubscribedHistoryResponse;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

        List<Subscribed> list = user.getSubscribedHistory();

        Subscribed latestSubscribed = list.stream()
                .max(Comparator.comparing(Subscribed::getCreatedAt))
                .orElse(null);

        // 현재 요금제와 같은지 체크
        if (latestSubscribed != null && latestSubscribed.getPlan().equals(plan)) {
            throw new GeneralException(PlanException.ALREADY_SUBSCRIBED_PLAN);
        }

        user.addSubscribed(Subscribed.of(plan));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<ShowSubscribedHistoryResponse> findSubscribedHistoryByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
        List<Subscribed> subscribeds = user.getSubscribedHistory();

        return subscribeds.stream()
                .sorted(Comparator.comparing(Subscribed::getCreatedAt).reversed())
                .map(ShowSubscribedHistoryResponse::from)
                .collect(Collectors.toList());
    }
}
