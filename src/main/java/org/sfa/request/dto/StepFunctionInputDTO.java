package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for Step Function input when a request is created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepFunctionInputDTO {
    private String requestId;
    private String requesterId;
    private String requestDescription;
    private String requestPriority;
    private String requestType;
    private String requestStatus;
    private String requestCategory;
    private String city;
    private String zipCode;
    private ZonedDateTime submittedAt;
}
