package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequestDTO {

    private String requestId;
    private String subject;
    private String creatorId;
    private String beneficiaryId;
    private String leadVolunteerId;
    private String category;
    private String status;
    private String priority;
    private ZonedDateTime lastUpdated;
}