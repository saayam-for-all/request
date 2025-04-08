package org.sfa.request.service;

import org.sfa.request.dto.CreateSaayamRequestDTO;
import org.sfa.request.model.SaayamRequest;

public interface SaayamRequestService {
    /**
     * Creates a new Saayam request
     * @param requestDTO The request data
     * @return The created Saayam request
     * @throws IllegalArgumentException if the request is fraudulent
     */
    SaayamRequest createRequest(CreateSaayamRequestDTO requestDTO);
} 