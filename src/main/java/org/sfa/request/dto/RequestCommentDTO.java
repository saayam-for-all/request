package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCommentDTO {

    private Long id;

    private String requestId;

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
