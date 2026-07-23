package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sfa.request.model.entity.*;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Lightweight projection of Request used for the dashboard/list endpoint
 * (GET /requests). Intentionally omits requestType, per issue #14 -
 * the dashboard list no longer shows a Type column. Use the full Request
 * entity (GET /requests/{requestId}) for the request-details view, which
 * still needs requestType.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSummaryDTO {

    private String requestId;
    private String requesterId;
    private RequestStatus requestStatus;
    private RequestPriority requestPriority;
    private HelpCategory helpCategory;
    private RequestFor requestFor;
    private String city;
    private String zipCode;
    private String requestSubject;
    private String requestDescription;
    private String audioRequestDescription;
    private RequestIsLeadVol requestIsLeadVol;
    private String leadVolunteerUserId;
    private List<String> helpingVolunteerUserIds;
    private ZonedDateTime submittedAt;
    private ZonedDateTime servicedAt;
    private ZonedDateTime lastUpdatedAt;

    public static RequestSummaryDTO fromEntity(Request request) {
        return RequestSummaryDTO.builder()
                .requestId(request.getRequestId())
                .requesterId(request.getRequesterId())
                .requestStatus(request.getRequestStatus())
                .requestPriority(request.getRequestPriority())
                .helpCategory(request.getHelpCategory())
                .requestFor(request.getRequestFor())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .requestSubject(request.getRequestSubject())
                .requestDescription(request.getRequestDescription())
                .audioRequestDescription(request.getAudioRequestDescription())
                .requestIsLeadVol(request.getRequestIsLeadVol())
                .leadVolunteerUserId(request.getLeadVolunteerUserId())
                .helpingVolunteerUserIds(request.getHelpingVolunteerUserIds())
                .submittedAt(request.getSubmittedAt())
                .servicedAt(request.getServicedAt())
                .lastUpdatedAt(request.getLastUpdatedAt())
                .build();
    }
}
