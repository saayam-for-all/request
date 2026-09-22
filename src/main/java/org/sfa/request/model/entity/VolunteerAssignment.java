package org.sfa.request.model.entity;

import lombok.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * ClassName: VolunteerAssignment
 * Package: org.sfa.request.model.entity
 * Description:
 *
 * Maps to virginia_dev_saayam_rdbms.volunteers_assigned. One row per volunteer
 * assigned to a request; volunteerType distinguishes the LEAD volunteer (at most
 * one per request) from HELPING volunteers (zero or more per request).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "volunteers_assigned", schema = "virginia_dev_saayam_rdbms")
public class VolunteerAssignment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "volunteers_assigned_id", updatable = false, nullable = false)
    private Long volunteersAssignedId;

    @Column(name = "request_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String requestId;

    @Column(name = "volunteer_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String volunteerId;

    @Column(name = "volunteer_type", nullable = false, columnDefinition = "VARCHAR(255)")
    private String volunteerType;

    @Column(name = "last_update_date", nullable = false, columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdateDate;
}
