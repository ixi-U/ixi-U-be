package com.ixi_U.benefit.controller;

import com.ixi_U.benefit.dto.request.SaveBundledBenefitRequest;
import com.ixi_U.benefit.dto.request.SaveSingleBenefitRequest;
import com.ixi_U.benefit.dto.response.FindBundledBenefitResponse;
import com.ixi_U.benefit.dto.response.FindSingleBenefitResponse;
import com.ixi_U.benefit.service.BenefitService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/api/benefits/bundled")
    public ResponseEntity<List<FindBundledBenefitResponse>> findAllBundledBenefits() {

        List<FindBundledBenefitResponse> allBundledBenefit = benefitService.findAllBundledBenefit();

        return ResponseEntity.ok(allBundledBenefit);
    }

    @GetMapping("/api/benefits/single")
    public ResponseEntity<List<FindSingleBenefitResponse>> findAllSingleBenefits() {

        List<FindSingleBenefitResponse> allSingleBenefit = benefitService.findAllSingleBenefit();

        return ResponseEntity.ok(allSingleBenefit);
    }

    @PostMapping("/api/benefits/single")
    public ResponseEntity<Void> saveSingleBenefit(
            @RequestBody @Valid SaveSingleBenefitRequest request) {

        benefitService.saveSingleBenefit(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/benefits/bundled")
    public ResponseEntity<Void> saveBundledBenefit(
            @RequestBody @Valid SaveBundledBenefitRequest request) {

        benefitService.saveBundledBenefit(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
