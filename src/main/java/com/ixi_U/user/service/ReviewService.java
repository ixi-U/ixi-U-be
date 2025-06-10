package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.exception.UserException;
import com.ixi_U.user.repository.ReviewedRepository;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewedRepository reviewedRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscribedRepository subscribedRepository;


    @Transactional
    public void createReview(String userId, CreateReviewRequest createReviewRequest) {

        Plan findPlan = planRepository.findById(createReviewRequest.planId())
                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

        User findUser = userRepository.findById(userId).orElseThrow(() -> new GeneralException(
                UserException.USER_NOT_FOUND));

        checkEnrollReview(findPlan, findUser);

        Reviewed createdReview = Reviewed.of(createReviewRequest.point(),
                findPlan,
                createReviewRequest.comment());

        findUser.addReviewed(createdReview);
        userRepository.save(findUser);

    }

    private void checkEnrollReview(Plan findPlan, User findUser) {

        // 구독을 이미 했던 요금제가 아니라면 리뷰 불가
        if (!subscribedRepository.existsSubscribeRelation(findUser.getId(), findPlan.getId())) {
            throw new GeneralException(SubscribedException.PLAN_NOT_SUBSCRIBED);
        }

        // 이미 작성된 리뷰가 있다면 리뷰 불가
        if (reviewedRepository.existsReviewedRelation(findUser.getId(), findPlan.getId())) {
            throw new GeneralException(ReviewedException.REVIEW_ALREADY_EXIST);
        }
    }

}
