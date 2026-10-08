package org.sfa.request.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestDetailsDTO {

    private String reqFname;

    private String reqLname;

    @Email(message = "Invalid email format")
    private String reqEmail;

    @NotBlank(message = "Phone number is required")
    private String reqPhone;

    private Integer reqAge;

    private String reqGender;

    private String reqPrefLang;
}
