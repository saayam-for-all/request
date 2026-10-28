package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerAssignmentDTO {

    @NotBlank(message = "Volunteer ID cannot be blank")
    private String volunteerId;

    @NotNull(message = "Volunteer assignment type cannot be null")
    private VolunteerAssignmentTypeEnum volunteerType;
}
