package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepFunctionInputDTO {

    private String requestId;
    private String requesterId;
    private Integer requestTypeId;

    private String helpCategoryId;
    private String helpCategoryName;

    private String requestSubject;
    private String requestDescription;
    private String requestLocation;

    private Double latitude;
    private Double longitude;

    private Integer volunteerLimit;
    private Double volunteerRadius;
    private String distanceUnit;
    
}