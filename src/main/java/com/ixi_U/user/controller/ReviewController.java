package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.request.UpdateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Void> createReview(@AuthenticationPrincipal String userId,
            @RequestBody @Valid CreateReviewRequest createReviewRequest) {

        reviewService.createReview(userId, createReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ShowReviewListResponse> showReviewList(
            @RequestParam("planId") final String planId, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(reviewService.showReview(planId, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<ShowReviewStatsResponse> showReviewStats(
            @RequestParam("planId") final String planId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(reviewService.showReviewStats(planId));
    }

    @PatchMapping
    public ResponseEntity<Void> updateReview(@AuthenticationPrincipal String userId,@RequestBody @Valid
            UpdateReviewRequest updateReviewRequest){

        reviewService.updateReview(userId, updateReviewRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
