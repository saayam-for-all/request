package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "request_notes")
public class RequestNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id", updatable = false)
    private Long noteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", referencedColumnName = "req_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rn_request"))
    private Request request;

    @Column(name = "author_user_id")
    private String authorUserId;

    @Column(name = "persona")
    private String persona; // e.g., REQUESTOR, LEAD_VOLUNTEER, HELPER, ADMIN

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}
