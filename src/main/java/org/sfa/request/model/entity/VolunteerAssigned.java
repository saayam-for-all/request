package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "volunteers_assigned")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerAssigned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vol_assigned_id")
    private Integer volunteerAssignedId;

    @Column(name = "req_id", nullable = false)
    private String requestId;

    @Column(name = "volunteer_id", nullable = false)
    private String volunteerId;

    @Column(name = "volunteer_type", nullable = false)
    private String volunteerType;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}