package org.sfa.request.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RequestPriority")
@Table(
        name = "request_priority",
        schema = "virginia_dev_saayam_rdbms",
        uniqueConstraints = {
                @UniqueConstraint(name = "request_priority_id_unique", columnNames = "request_priority_id")
        }
)
public class RequestPriority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_priority_id", updatable = false)
    private Integer priorityId;

    @Column(name = "req_priority", nullable = false, columnDefinition = "VARCHAR(255)")
    private String priority;

    @Column(name = "req_priority_desc", columnDefinition = "VARCHAR(255)")
    private String description;

    @Column(name = "last_updated_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdatedAt;

    @JsonIgnore
    @OneToMany(
            mappedBy = "requestPriority",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Request> requests = new HashSet<>();
}
