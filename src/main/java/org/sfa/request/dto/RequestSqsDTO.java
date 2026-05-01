package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: RequestSqsDTO
 * Package: org.sfa.request.dto
 * Description: DTO for sending request messages to SQS
 *
 * @author Fan Peng
 * Create 2024/8/15 3:01
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSqsDTO {
    private String requestId;
    private String requesterId;
    private String requestDescription;
    private String requestStatus;
    private String requestPriority;
    private String requestType;
    private String requestCategory;
    private String helpCategory;
    private String requestFor;
    private String city;
    private String zipCode;
    private String audioRequestDescription;
    private String submittedAt;
    private Integer leadVolunteerUserId;
    private String servicedAt;
    private String lastUpdatedAt;
}