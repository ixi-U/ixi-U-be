package com.ixi_U.benefit.service;

import com.ixi_U.benefit.dto.response.FindBundledBenefitResponse;
import com.ixi_U.benefit.dto.response.FindSingleBenefitResponse;
import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.benefit.repository.BundledBenefitRepository;
import com.ixi_U.benefit.repository.SingleBenefitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final BundledBenefitRepository bundledBenefitRepository;
    private final SingleBenefitRepository singleBenefitRepository;

    /**
     * 묶음 혜택 조회
     */
    public List<FindBundledBenefitResponse> findAllBundledBenefit() {

        List<BundledBenefit> bundledBenefits = bundledBenefitRepository.findAll();

        return bundledBenefitEntityToDto(bundledBenefits);
    }

    /**
     * 단일 혜택 조회
     */
    public List<FindSingleBenefitResponse> findAllSingleBenefit() {

        List<SingleBenefit> singleBenefits = singleBenefitRepository.findAll();

        return singleBenefitEntityToDto(singleBenefits);
    }

    private List<FindBundledBenefitResponse> bundledBenefitEntityToDto(
            List<BundledBenefit> bundledBenefits) {

        return bundledBenefits.stream()
                .map(entity -> {

                    List<FindSingleBenefitResponse> findSingleBenefitResponses = singleBenefitEntityToDto(
                            entity.getSingleBenefits());

                    return FindBundledBenefitResponse.create(
                            entity.getId(),
                            entity.getName(),
                            entity.getSubscript(),
                            entity.getChoice(),
                            findSingleBenefitResponses);
                }).toList();
    }

    private List<FindSingleBenefitResponse> singleBenefitEntityToDto(
            List<SingleBenefit> singleBenefits) {

        return singleBenefits.stream()
                .map(entity ->
                        FindSingleBenefitResponse.create(
                                entity.getId(),
                                entity.getName(),
                                entity.getSubscript(),
                                entity.getBenefitType())
                ).toList();
    }
}
