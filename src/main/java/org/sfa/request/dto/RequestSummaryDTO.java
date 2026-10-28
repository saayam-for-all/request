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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSummaryDTO {

    private String requestId;
    private String creatorId;
    private String beneficiaryId;
    private String leadVolunteerId;

    private RequestStatus requestStatus;
    private RequestPriority requestPriority;
    private HelpCategory helpCategory;
    private RequestFor requestFor;

    private Boolean isCalamity;
    private String requestSubject;
    private String requestDescription;
    private String requestDocumentLink;
    private String audioRequestDescription;

    private RequestIsLeadVolunteer isLeadVolunteer;

    private ZonedDateTime submittedAt;
    private ZonedDateTime servicedAt;
    private ZonedDateTime lastUpdatedAt;

    public static RequestSummaryDTO fromEntity(Request request) {
        return RequestSummaryDTO.builder()
                .requestId(request.getRequestId())
                .creatorId(request.getCreatorId())
                .beneficiaryId(request.getBeneficiaryId())
                .leadVolunteerId(request.getLeadVolunteerId())
                .requestStatus(request.getRequestStatus())
                .requestPriority(request.getRequestPriority())
                .helpCategory(request.getHelpCategory())
                .requestFor(request.getRequestFor())
                .isCalamity(request.getIsCalamity())
                .requestSubject(request.getRequestSubject())
                .requestDescription(request.getRequestDescription())
                .requestDocumentLink(request.getRequestDocumentLink())
                .audioRequestDescription(request.getAudioRequestDescription())
                .isLeadVolunteer(request.getIsLeadVolunteer())
                .submittedAt(request.getSubmittedAt())
                .servicedAt(request.getServicedAt())
                .lastUpdatedAt(request.getLastUpdatedAt())
                .build();
    }
}