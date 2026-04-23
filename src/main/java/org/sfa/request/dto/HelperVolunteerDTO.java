package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelperVolunteerDTO {
    private String volunteerUserId;
    private String addedByUserId;
    private ZonedDateTime addedAt;
}
