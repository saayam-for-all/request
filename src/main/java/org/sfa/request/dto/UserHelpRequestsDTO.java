package org.sfa.request.dto;

import lombok.Data;

@Data
public class UserHelpRequestsDTO {
    private String userId;
    private Integer page = 0;
    private Integer size = 10;
}