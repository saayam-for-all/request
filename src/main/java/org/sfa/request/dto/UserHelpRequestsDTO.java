package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHelpRequestsDTO {

    private String userId;
    private Integer page;
    private Integer size;
}