package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: RequestSqsDTO
 * Package: org.sfa.request.dto
 * Description:
 *
 * @author Fan Peng
 * Create 2024/8/15 3:01
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSqsDTO {
    private String requestId;
    private String requesterId;
    private String requestStatus;
    private String requestPriority;
    private String requestType;
    private String requestCategory;
    private String helpCategory;
    private String requestFor;
    private String city;
    private String zipCode;
    private String requestDescription;
    private String audioRequestDescription;
    private String submittedAt;
    private Integer leadVolunteerUserId;
    private String servicedAt;
    private String lastUpdatedAt;
}