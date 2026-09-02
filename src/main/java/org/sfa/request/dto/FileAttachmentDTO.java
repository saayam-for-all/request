package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachmentDTO {

    @NotBlank(message = "fileName is required for each attachment")
    private String fileName;

    @NotBlank(message = "base64 content is required for each attachment")
    private String base64;

    @NotBlank(message = "contentType is required for each attachment")
    private String contentType;
}