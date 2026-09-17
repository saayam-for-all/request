package org.sfa.request.dto;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    @NotBlank(message = "Requester ID cannot be blank")
    @Size(max = 255, message = "Requester ID must not exceed 255 characters")
    private String requesterId;

    @NotBlank(message = "Request subject cannot be blank")
    @Size(max = 125, message = "Request subject must not exceed 125 characters")
    private String requestSubject;

    @NotBlank(message = "Request description cannot be blank")
    @Size(max = 255, message = "Request description must not exceed 255 characters")
    private String requestDescription;

    @Size(max = 255, message = "Audio request description must not exceed 255 characters")
    private String audioRequestDescription;

    @Size(max = 125, message = "Request location must not exceed 125 characters")
    private String requestLocation;

    // NEW: matches iscalamity in DB
    private Boolean isCalamity;

    // NEW: matches req_doc_link in DB
    private String requestDocumentLink;

    private ZonedDateTime submittedAt;
    private ZonedDateTime servicedAt;
    private ZonedDateTime lastUpdatedAt;

    // maps to req_islead_id
    private Integer isLeadVolunteer;

    @Valid
    private GuestDetailsDTO guestDetails;

    @NotNull(message = "Request status cannot be null")
    private RequestStatusDTO requestStatus;

    @NotNull(message = "Request priority cannot be null")
    private RequestPriorityDTO requestPriority;

    @NotNull(message = "Request type cannot be null")
    private RequestTypeDTO requestType;

    @NotNull(message = "Help category cannot be null")
    private HelpCategoryDto helpCategory;

    @NotNull(message = "Request for cannot be null")
    private RequestForDTO requestFor;

    @JsonProperty("additionalFields")
    private Map<String, Object> additionalFields;

    @Valid
    @Size(max = 5, message = "Maximum 5 attachments allowed")
    private List<FileAttachmentDTO> files;
}
