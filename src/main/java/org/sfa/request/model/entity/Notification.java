package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.time.ZonedDateTime;

/**
 * Row in the shared {@code notifications} table.
 *
 * <p>Written by this service's notification consumer; read by the Volunteer
 * microservice's notification APIs. The table is shared by design — see the
 * "Notifications Microservice Requirements" note in the volunteer wiki.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications", schema = "virginia_dev_saayam_rdbms")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", updatable = false)
    private Integer notificationId;

    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId;

    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "channel_id", nullable = false)
    private Integer channelId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Maps to the PostgreSQL enum {@code status_type('unread','read')}. Hibernate
     * binds a String, so the write is explicitly cast to the enum type.
     */
    @Column(name = "status", columnDefinition = "status_type")
    @ColumnTransformer(write = "?::status_type")
    private String status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(name = "last_update_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdateDate;
}
