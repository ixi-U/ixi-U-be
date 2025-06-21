package com.ixi_U.user.controller;

import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(@AuthenticationPrincipal String userId,
            @RequestParam("reviewId")Long reviewId) {

        reportService.createReport(userId,reviewId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ShowReviewListResponse showReport(Pageable pageable){

        return reportService.showReport(pageable);
    }

}
