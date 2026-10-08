package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearestVolunteersRequest {

    private Double latitude;
    private Double longitude;
    private Integer limit;
    private Double radius;
    private String unit;
}