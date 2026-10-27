package org.sfa.request.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "req_comments",
        schema = "virginia_dev_saayam_rdbms"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "req_id", nullable = false)
    private String requestId;

    @Column(name = "comment_desc", nullable = false)
    private String comment;

    @Column(name = "commenter_id")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isdeleted")
    private Boolean isDeleted;
}