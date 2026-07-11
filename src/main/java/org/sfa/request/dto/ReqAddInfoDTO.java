package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqAddInfoDTO {

    @NotBlank(message = "Additional information field ID cannot be blank")
    private String fieldId;

    private String fieldValue;

    private List<String> selectedItems;
}
