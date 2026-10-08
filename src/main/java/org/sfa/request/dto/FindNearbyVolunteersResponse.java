package org.sfa.request.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FindNearbyVolunteersResponse {
    private Integer statusCode;
    private Map<String, String> headers;
    private List<NearestVolunteerDto> body;
}