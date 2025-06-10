package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> createReview(@RequestParam("userId") String userId,
            @RequestBody @Valid CreateReviewRequest createReviewRequest) {

        reviewService.createReview(userId, createReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
