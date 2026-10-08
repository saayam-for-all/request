package org.sfa.request.dto;

import lombok.Data;

@Data
public class NearestVolunteerDto {
    private String name;
    private String cause;
    private String phone;
    private String email;
    private String location;
    private String rating;
    private String dateAdded;
}