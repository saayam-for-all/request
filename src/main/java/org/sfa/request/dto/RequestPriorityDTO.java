package org.sfa.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPriorityDTO {
    @NotNull(message = "Request priority ID cannot be null")
    private Integer requestPriorityId;
}

