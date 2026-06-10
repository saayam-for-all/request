package org.sfa.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusDTO {
    @NotNull(message = "Request status ID cannot be null")
    private Integer requestStatusId;
}
