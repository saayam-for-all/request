package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "request_volunteers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_rv_req_vol", columnNames = {"req_id", "volunteer_user_id"})
        }
)
public class RequestVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_volunteer_id", updatable = false)
    private Long requestVolunteerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", referencedColumnName = "req_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rv_request"))
    private Request request;

    @Column(name = "volunteer_user_id", nullable = false)
    private String volunteerUserId;

    @Column(name = "added_by_user_id")
    private String addedByUserId;

    @Column(name = "added_at")
    private ZonedDateTime addedAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (addedAt == null) {
            addedAt = ZonedDateTime.now();
        }
    }
}
