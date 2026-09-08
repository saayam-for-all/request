package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    @NotBlank(message = "Requester ID cannot be blank")
    private String requesterId;

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
}