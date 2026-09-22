package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_types", schema = "virginia_dev_saayam_rdbms")
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id", updatable = false)
    private Integer typeId;

    @Column(name = "type_name", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
