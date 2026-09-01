package org.sfa.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCategoryDTO {
    @NotNull(message = "Request category ID cannot be null")
    private Integer requestCategoryId;
}

