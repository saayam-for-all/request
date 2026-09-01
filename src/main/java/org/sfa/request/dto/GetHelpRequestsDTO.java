package org.sfa.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetHelpRequestsDTO {

    private String requestId;
    private String creatorId;
    private String beneficiaryId;
    private String status;
    private String subject;
    private ZonedDateTime updatedDate;
    private ZonedDateTime creationDate;
    private String type;
    private String requestCategory;
    private String reqCatId;
    private String reqDesc;
    private String priority;
    private Boolean calamity;
    private Integer reqForId;
    private Integer reqIsleadId;

    public GetHelpRequestsDTO(
            String requestId,
            String status,
            String subject,
            ZonedDateTime updatedDate,
            ZonedDateTime creationDate,
            String type,
            String requestCategory,
            String reqCatId,
            String reqDesc,
            String priority,
            Boolean calamity
    ) {
        this.requestId = requestId;
        this.status = status;
        this.subject = subject;
        this.updatedDate = updatedDate;
        this.creationDate = creationDate;
        this.type = type;
        this.requestCategory = requestCategory;
        this.reqCatId = reqCatId;
        this.reqDesc = reqDesc;
        this.priority = priority;
        this.calamity = calamity;
    }
}