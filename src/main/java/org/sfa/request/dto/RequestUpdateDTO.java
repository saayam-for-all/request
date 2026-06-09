package org.sfa.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateDTO {

    @Valid
    @NotNull(message = "requestPriority is required")
    private String requestId;

    @Valid
    @NotNull(message = "requestType is required")
    private String requesterId;

    @Size(max = 125, message = "Request subject must not exceed 125 characters")
    private String requestSubject;

    @Size(max = 255, message = "Request description must not exceed 255 characters")
    private String requestDescription;

    @Size(max = 255, message = "Audio request description must not exceed 255 characters")
    private String audioRequestDescription;

    @Size(max = 125, message = "Request location must not exceed 125 characters")
    private String requestLocation;

    private Boolean isCalamity;

    private String requestDocumentLink;

    private ZonedDateTime servicedAt;

    private Integer isLeadVolunteer;

    @Valid
    private RequestStatusDTO requestStatus;

    @Valid
    private RequestPriorityDTO requestPriority;

    @Valid
    private RequestTypeDTO requestType;

    @Valid
    private HelpCategoryDto helpCategory;

    @Valid
    private RequestForDTO requestFor;

    @Valid
    private GuestDetailsDTO guestDetails;

    @JsonProperty("additionalFields")
    private Map<String, Object> additionalFields;
}