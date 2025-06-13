package com.ixi_U.benefit.controller;

import com.ixi_U.benefit.dto.response.FindBundledBenefitResponse;
import com.ixi_U.benefit.dto.response.FindSingleBenefitResponse;
import com.ixi_U.benefit.service.BenefitService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
