package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.forbiddenWord.ReviewFilter;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.request.UpdateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.dto.response.ShowReviewSummaryResponse;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.exception.UserException;
import com.ixi_U.user.repository.ReviewedRepository;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewedRepository reviewedRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscribedRepository subscribedRepository;
    private final ReviewFilter reviewFilter;


    @Transactional
    public void createReview(String userId, CreateReviewRequest createReviewRequest) {

        Plan findPlan = planRepository.findById(createReviewRequest.planId())
                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

        User findUser = userRepository.findById(userId).orElseThrow(() -> new GeneralException(
                UserException.USER_NOT_FOUND));

        checkEnrollReview(findPlan, findUser, createReviewRequest.comment());

        Reviewed createdReview = Reviewed.of(createReviewRequest.point(),
                findPlan,
                createReviewRequest.comment());

        findUser.addReviewed(createdReview);
        userRepository.save(findUser);

    }

    private void checkEnrollReview(Plan findPlan, User findUser,String comment) {

        // 구독을 이미 했던 요금제가 아니라면 리뷰 불가
        if (!subscribedRepository.existsSubscribeRelation(findUser.getId(), findPlan.getId())) {
            throw new GeneralException(SubscribedException.PLAN_NOT_SUBSCRIBED);
        }

        // 이미 작성된 리뷰가 있다면 리뷰 불가
        if (reviewedRepository.existsReviewedRelation(findUser.getId(), findPlan.getId())) {
            throw new GeneralException(ReviewedException.REVIEW_ALREADY_EXIST);
        }

        // 리뷰 내용에 금칙어 존재 시 리뷰 불가.
        if(reviewFilter.matches(comment)){
            throw new GeneralException(ReviewedException.REVIEW_FIND_FORBIDDEN_WORD);
        }
    }

    public ShowReviewListResponse showReview(String planId, Pageable pageable) {

        Slice<ShowReviewResponse> reviewedList = reviewedRepository.findReviewedByPlanWithPaging(
                planId, pageable);

        return ShowReviewListResponse.of(reviewedList.getContent(), reviewedList.hasNext());
    }

    public ShowReviewSummaryResponse showReviewSummary(String userId, String planId) {

        ShowReviewResponse myReviewResponse = null;
        if(userId!=null && !userId.equals("anonymousUser")){
            myReviewResponse = reviewedRepository.showMyReview(userId,planId);
        }

        return ShowReviewSummaryResponse.of(userRepository.findAveragePointAndReviewCount(planId),myReviewResponse);
    }

    @Transactional
    public void updateReview(String userId, UpdateReviewRequest updateReviewRequest){

        checkUpdateReview(userId,updateReviewRequest.reviewId(),updateReviewRequest.comment());
        reviewedRepository.updateReviewed(updateReviewRequest.reviewId(), updateReviewRequest.comment());
    }

    private void checkUpdateReview(String userId, Long reviewId,String comment){

        checkReviewOwner(userId, reviewId);

        if(reviewFilter.matches(comment)){
            throw new GeneralException(ReviewedException.REVIEW_FIND_FORBIDDEN_WORD);
        }
    }

    private void checkReviewOwner(String userId, Long reviewId) {

        User reviewedOner = userRepository.findOwnerByReviewedId(reviewId)
                .orElseThrow(()->new GeneralException(ReviewedException.REVIEW_NOT_FOUND));

        User findUser = userRepository.findById(userId).orElseThrow(() -> new GeneralException(
                UserException.USER_NOT_FOUND));

        if(!reviewedOner.getId().equals(findUser.getId())){
            throw new GeneralException(ReviewedException.REVIEW_NOT_OWNER);
        }
    }

    @Transactional
    public void delete(String userId, Long reviewId){

        checkReviewOwner(userId,reviewId);

        reviewedRepository.deleteReviewedById(reviewId);
    }

}
