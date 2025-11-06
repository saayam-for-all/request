package org.sfa.request.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestDetailsDTO {

    @NotBlank(message = "First name is required")
    private String reqFname;

    @NotBlank(message = "Last name is required")
    private String reqLname;

    @Email(message = "Invalid email format")
    private String reqEmail;

    @NotBlank(message = "Phone number is required")
    private String reqPhone;

    private Integer reqAge;

    private String reqGender;

    private String reqPrefLang;
}
