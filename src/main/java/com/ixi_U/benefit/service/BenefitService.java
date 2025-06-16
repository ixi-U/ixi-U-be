package com.ixi_U.benefit.service;

import com.ixi_U.benefit.dto.request.SaveBundledBenefitRequest;
import com.ixi_U.benefit.dto.request.SaveSingleBenefitRequest;
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

    /**
     * 단일 혜택 저장
     */
    public void saveSingleBenefit(final SaveSingleBenefitRequest request) {

        SingleBenefit singleBenefit = singleBenefitDtoToEntity(request);

        singleBenefitRepository.save(singleBenefit);
    }

    /**
     * 묶음 혜택 저장
     */
    public void saveBundledBenefit(final SaveBundledBenefitRequest request) {

        BundledBenefit bundledBenefit = bundledBenefitDtoToEntity(request);

        bundledBenefitRepository.save(bundledBenefit);
    }

    private BundledBenefit bundledBenefitDtoToEntity(final SaveBundledBenefitRequest request) {

        List<String> singleBenefitIds = request.singleBenefitIds();

        List<SingleBenefit> singleBenefits = singleBenefitRepository.findAllById(singleBenefitIds);

        BundledBenefit bundledBenefit = BundledBenefit.create(
                request.name(),
                request.subscript(),
                request.choice()
        );

        bundledBenefit.addAllSingleBenefit(singleBenefits);

        return bundledBenefit;
    }

    private SingleBenefit singleBenefitDtoToEntity(final SaveSingleBenefitRequest request) {

        return SingleBenefit.create(request.name(), request.subscript(), request.benefitType());
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
