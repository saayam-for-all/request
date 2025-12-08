package org.sfa.request.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.sfa.request.model.enums.RequestPriorityEnum;
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
        uniqueConstraints = {
                @UniqueConstraint(name = "req_priority_id_unique", columnNames = "req_priority_id")
        }
)
public class RequestPriority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_priority_id", updatable = false)
    private Integer priorityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "req_priority", nullable = false, columnDefinition = "VARCHAR(255)")
    private RequestPriorityEnum priority;

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

