package org.sfa.request.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    @NotBlank(message = "Requester ID cannot be blank")
    @Size(max = 255, message = "Requester ID must not exceed 255 characters")
    private String requesterId;

    @NotBlank(message = "Request description cannot be blank")
    @Size(max = 255, message = "Request description must not exceed 255 characters")
    private String requestDescription;

    @Size(max = 255, message = "Audio request description must not exceed 255 characters")
    private String audioRequestDescription;

    @Size(max = 255, message = "City name must not exceed 255 characters")
    private String city;

    @Size(max = 20, message = "ZIP code must not exceed 20 characters")
    private String zipCode;

    private ZonedDateTime submittedAt;

    private Integer leadVolunteerUserId;

    private ZonedDateTime servicedAt;

    private ZonedDateTime lastUpdatedAt;

    @NotNull(message = "Request status cannot be null")
    private RequestStatusDTO requestStatus;

    @NotNull(message = "Request priority cannot be null")
    private RequestPriorityDTO requestPriority;

    // Optional: defaults to REMOTE in RequestServiceImpl when not supplied
    private RequestTypeDTO requestType;
    
    @NotNull(message = "Request category cannot be null")
    private HelpCategoryDto helpCategory;
	/*
	 * @NotNull(message = "Request category cannot be null") private
	 * RequestCategoryDTO requestCategory;
	 */

    @NotNull(message = "Request for cannot be null")
    private RequestForDTO requestFor;
    
    @NotNull(message = "requestIsLeadVol cannot be null")
    private RequestIsLeadVolDTO requestIsLeadVol;
    

    
}