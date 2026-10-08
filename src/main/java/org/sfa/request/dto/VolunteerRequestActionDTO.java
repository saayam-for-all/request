package org.sfa.request.dto;

import lombok.Data;

@Data
public class VolunteerRequestActionDTO {

    private String requestId;
    private String volunteerId;
    private String action;
}