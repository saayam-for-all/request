package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Lightweight projection of Request used for the dashboard/list endpoint
 * (GET /requests). Intentionally omits requestType, per issue #14 - the
 * dashboard list no longer shows a Type column. Use the full Request entity
 * (GET /requests/{requestId}) for the request-details view, which still
 * needs requestType.
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
    private String requestLocation;
    private Boolean isCalamity;
    private String requestSubject;
    private String requestDescription;
    private String requestDocumentLink;
    private String audioRequestDescription;
    private RequestIsLeadVolunteer isLeadVolunteer;
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
                .requestLocation(request.getRequestLocation())
                .isCalamity(request.getIsCalamity())
                .requestSubject(request.getRequestSubject())
                .requestDescription(request.getRequestDescription())
                .requestDocumentLink(request.getRequestDocumentLink())
                .audioRequestDescription(request.getAudioRequestDescription())
                .isLeadVolunteer(request.getIsLeadVolunteer())
                .leadVolunteerUserId(request.getLeadVolunteerUserId())
                .helpingVolunteerUserIds(request.getHelpingVolunteerUserIds())
                .submittedAt(request.getSubmittedAt())
                .servicedAt(request.getServicedAt())
                .lastUpdatedAt(request.getLastUpdatedAt())
                .build();
    }
}
