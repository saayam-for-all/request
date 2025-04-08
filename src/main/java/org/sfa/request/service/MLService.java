package org.sfa.request.service;

import org.sfa.request.dto.CreateSaayamRequestDTO;

public interface MLService {
    /**
     * Checks if the request is fraudulent
     * @param request The request to check
     * @return true if the request is fraudulent, false otherwise
     */
    boolean isFraudulentRequest(CreateSaayamRequestDTO request);

    /**
     * Translates the request description to the required language
     * @param description The description to translate
     * @param targetLanguage The target language code
     * @return The translated description
     */
    String translateDescription(String description, String targetLanguage);
} 