package org.sfa.request.service.impl;

import org.sfa.request.dto.CreateSaayamRequestDTO;
import org.sfa.request.model.SaayamRequest;
import org.sfa.request.repository.SaayamRequestRepository;
import org.sfa.request.service.MLService;
import org.sfa.request.service.SaayamRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaayamRequestServiceImpl implements SaayamRequestService {

    private final SaayamRequestRepository requestRepository;
    private final MLService mlService;

    public SaayamRequestServiceImpl(SaayamRequestRepository requestRepository, MLService mlService) {
        this.requestRepository = requestRepository;
        this.mlService = mlService;
    }

    @Override
    @Transactional
    public SaayamRequest createRequest(CreateSaayamRequestDTO requestDTO) {
        // Check for fraudulent request
        if (mlService.isFraudulentRequest(requestDTO)) {
            throw new IllegalArgumentException("Fraudulent request detected");
        }

        // Create new request entity
        SaayamRequest request = new SaayamRequest();
        request.setRequesterId(requestDTO.getRequesterId());
        request.setRequesterName(requestDTO.getRequesterName());
        request.setBeneficiaryId(requestDTO.getBeneficiaryId());
        request.setBeneficiaryName(requestDTO.getBeneficiaryName());
        request.setRequestType(requestDTO.getRequestType());
        request.setDescription(requestDTO.getDescription());
        request.setStatus("PENDING");

        // Save the request
        return requestRepository.save(request);
    }
} 