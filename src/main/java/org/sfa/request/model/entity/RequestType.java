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
@Entity(name = "RequestType")
@Table(
        name = "request_type",
        schema = "virginia_dev_saayam_rdbms",
        uniqueConstraints = {
                @UniqueConstraint(name = "request_type_id_unique", columnNames = "request_type_id")
        }
)
public class RequestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_type_id", updatable = false)
    private Integer requestTypeId;

    @Column(name = "req_type", nullable = false, columnDefinition = "VARCHAR(255)")
    private String type;

    @Column(name = "req_type_desc", columnDefinition = "VARCHAR(255)")
    private String description;

    @Column(name = "last_updated_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdatedAt;

    @JsonIgnore
    @OneToMany(
            mappedBy = "requestType",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Request> requests = new HashSet<>();
}
