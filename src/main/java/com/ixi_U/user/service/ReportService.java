package com.ixi_U.user.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.entity.Reported;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.exception.UserException;
import com.ixi_U.user.repository.ReportedRepository;
import com.ixi_U.user.repository.ReviewedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportedRepository reportedRepository;
    private final ReviewedRepository reviewedRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createReport(String userId,Long reviewId){

        validateReport(userId,reviewId);

        reportedRepository.save(Reported.from(reviewId));
    }

    private void validateReport(String userId, Long reviewId){

        User reviewedOner = userRepository.findOwnerByReviewedId(reviewId)
                .orElseThrow(()->new GeneralException(ReviewedException.REVIEW_NOT_FOUND));

        User findUser = userRepository.findById(userId).orElseThrow(() -> new GeneralException(
                UserException.USER_NOT_FOUND));

        if(reviewedOner.getId().equals(findUser.getId())){
            throw new GeneralException(ReviewedException.REVIEW_REPORT_ONLY_OTHER);
        }
    }

    public ShowReviewListResponse showReport(Pageable pageable){

        Slice<Reported> reportedWithPaging = reportedRepository.findReportedWithSlice(pageable);

        List<ShowReviewResponse> showReviewResponses = reportedWithPaging.getContent().stream().map(reported -> {
            User reviewOwner = userRepository.findOwnerAndReviewByReviewedId(reported.getReviewedId()).orElseThrow(()->new GeneralException(
                    UserException.USER_NOT_FOUND));

            if(reviewOwner.getReviewedHistory().isEmpty()){
                throw new GeneralException(ReviewedException.REVIEW_NOT_FOUND);
            }
            // null 방지 위한 Empty 검사
            Reviewed reviewed = reviewOwner.getReviewedHistory().get(0);

            return ShowReviewResponse.of(reviewed.getId(),reviewOwner.getName(),reviewed.getPoint(),reviewed.getComment(),reviewed.getCreatedAt());
        }).toList();

        return ShowReviewListResponse.of(showReviewResponses,reportedWithPaging.hasNext());
    }

}
