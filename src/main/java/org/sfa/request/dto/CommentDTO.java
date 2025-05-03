package org.sfa.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    @NotNull
    private Long commentId;

    @NotBlank
    @Size(max = 100, message = "Author name should not exceed 100 characters for an entry")
    private String authorName;

    @NotBlank
    @Size(max = 500, message = "Comment text should not exceed 500 characters for one single comment")
    private String commentText;

    public CommentDTO(String authorName, String commentText) {
        this.authorName = authorName;
        this.commentText = commentText;
    }
}
