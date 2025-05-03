package org.sfa.request.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @NotBlank
    @Size(max = 100)
    @Column(name= "author_name", nullable = false, columnDefinition = "VARCHAR(255)", length = 100)
    private String authorName;

    @NotBlank
    @Size(max = 500)
    @Column(name= "comment_text", nullable = false, columnDefinition = "VARCHAR(255)", length = 500)
    private String commentText;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_request_id"))
    private Request request;
}
