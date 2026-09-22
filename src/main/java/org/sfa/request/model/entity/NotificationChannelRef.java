package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Lookup row in {@code notification_channels}. Named ...Ref to avoid colliding
 * with the {@code NotificationChannel} transport enum in the notification DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_channels", schema = "virginia_dev_saayam_rdbms")
public class NotificationChannelRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id", updatable = false)
    private Integer channelId;

    @Column(name = "channel_name", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String channelName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
