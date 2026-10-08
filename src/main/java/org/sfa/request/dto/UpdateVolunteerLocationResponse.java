package org.sfa.request.dto;

import lombok.Data;

@Data
public class UpdateVolunteerLocationResponse {
    private String message;
    private LocationData data;

    @Data
    public static class LocationData {
        private String user_id;
        private Double latitude;
        private Double longitude;
        private String timestamp;
        private String location;
    }
}