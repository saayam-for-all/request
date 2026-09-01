package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: RequestSqsDTO
 * Package: org.sfa.request.dto
 * Description:
 *
 * DTO used for sending Request payloads to SQS.
 * Updated to reflect new schema:
 *  - requestCategory ➝ helpCategory
 *  - city/zipCode ➝ requestLocation
 *  - leadVolunteerUserId ➝ isLeadVolunteer
 *  - added requestSubject
 *
 * @author Fan Peng
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSqsDTO {
    private String requestId;
    private String creatorId;
    private String beneficiaryId;
    private String requestStatus;
    private String requestPriority;
    private String requestType;
    private String helpCategory;
    private String requestFor;
    private String requestLocation;
    private String requestSubject;
    private String requestDescription;
    private String audioRequestDescription;
    private String submittedAt;
    private String isLeadVolunteer;
    private String servicedAt;
    private String lastUpdatedAt;
}
