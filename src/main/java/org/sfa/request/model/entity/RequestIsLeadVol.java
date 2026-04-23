package org.sfa.request.model.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Entity class representing the Request IsLead/Volunteer table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "request_isleadvol",
        uniqueConstraints = {
                @UniqueConstraint(name = "req_islead_id_unique", columnNames = "req_islead_id")
        }
)
public class RequestIsLeadVol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_islead_id", updatable = false)
    private Integer requestIsLeadId;

    @Column(name = "req_islead", nullable = false, length = 15)
    private String requestIsLead;  // YES or NO

    @Column(name = "req_islead_desc", length = 100)
    private String description;

    @Column(name = "last_updated_date")
    private ZonedDateTime lastUpdatedAt;

}
