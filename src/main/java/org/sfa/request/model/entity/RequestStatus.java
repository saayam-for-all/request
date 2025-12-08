package org.sfa.request.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.sfa.request.model.enums.RequestStatusEnum;
import lombok.*;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RequestStatus")
@Table(
        name = "request_status",
        uniqueConstraints = {
                @UniqueConstraint(name = "req_status_id_unique", columnNames = "req_status_id")
        }
)
public class RequestStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_status_id", updatable = false)
    private Integer requestStatusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "req_status", nullable = false, columnDefinition = "VARCHAR(255)")
    private RequestStatusEnum status;

    @Column(name = "req_status_desc", columnDefinition = "VARCHAR(255)")
    private String description;

    @Column(name = "last_updated_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdatedAt;

    @JsonIgnore
    @OneToMany(
            mappedBy = "requestStatus",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Request> requests = new HashSet<>();

    @JsonIgnore
    public Integer getId() {
        return this.requestStatusId;
    }
}

