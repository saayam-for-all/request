package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequesterDTO {

    @NotBlank(message = "Requester ID cannot be blank")
    private String requesterId;
}