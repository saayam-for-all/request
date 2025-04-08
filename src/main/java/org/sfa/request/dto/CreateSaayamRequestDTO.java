package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSaayamRequestDTO {
    @NotBlank(message = "Requester ID is required")
    private String requesterId;

    @NotBlank(message = "Requester name is required")
    private String requesterName;

    @NotBlank(message = "Beneficiary ID is required")
    private String beneficiaryId;

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "Request type is required")
    private String requestType;

    @NotBlank(message = "Description is required")
    private String description;
} 