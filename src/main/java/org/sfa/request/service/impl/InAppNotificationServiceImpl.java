package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Notification;
import org.sfa.request.model.entity.NotificationChannelRef;
import org.sfa.request.model.entity.NotificationType;
import org.sfa.request.repository.NotificationChannelRepository;
import org.sfa.request.repository.NotificationRepository;
import org.sfa.request.repository.NotificationTypeRepository;
import org.sfa.request.service.api.InAppNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;

/**
 * Writes in-app notifications into the shared {@code notifications} table.
 *
 * <p>{@code type_id} and {@code channel_id} are NOT NULL foreign keys, so the
 * {@code notification_types} and {@code notification_channels} lookup tables must be
 * seeded before this can run. See {@code db/notification_lookup_seed.sql}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImpl implements InAppNotificationService {

    /** Channel name expected in the {@code notification_channels} lookup table. */
    static final String IN_APP_CHANNEL_NAME = "IN_APP";

    /** Status value from the PostgreSQL {@code status_type} enum. */
    private static final String STATUS_UNREAD = "unread";

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationChannelRepository notificationChannelRepository;

    @Override
    @Transactional
    public void record(NotificationEvent event, NotificationRecipient recipient) {
        NotificationType type = notificationTypeRepository
                .findByTypeName(event.getEventType().name())
                .orElseThrow(() -> new IllegalStateException(
                        "notification_types is missing row '" + event.getEventType().name()
                                + "'. Seed the notification lookup tables."));

        NotificationChannelRef channel = notificationChannelRepository
                .findByChannelName(IN_APP_CHANNEL_NAME)
                .orElseThrow(() -> new IllegalStateException(
                        "notification_channels is missing row '" + IN_APP_CHANNEL_NAME
                                + "'. Seed the notification lookup tables."));

        ZonedDateTime now = ZonedDateTime.now();

        Notification notification = Notification.builder()
                .userId(recipient.getUserId())
                .typeId(type.getTypeId())
                .channelId(channel.getChannelId())
                .message(buildMessage(event))
                .status(STATUS_UNREAD)
                .createdAt(now)
                .lastUpdateDate(now)
                .build();

        notificationRepository.save(notification);

        log.info("Recorded in-app {} notification for request {} to user {}",
                event.getEventType(), event.getRequestId(), recipient.getUserId());
    }

    private String buildMessage(NotificationEvent event) {
        String subject = StringUtils.hasText(event.getSubject())
                ? event.getSubject()
                : "New help request";

        String category = event.getAttributes() != null
                ? event.getAttributes().get("helpCategory")
                : null;

        StringBuilder message = new StringBuilder("New help request");
        if (StringUtils.hasText(category)) {
            message.append(" in ").append(category);
        }
        message.append(": ").append(subject);
        if (StringUtils.hasText(event.getRequestId())) {
            message.append(" (").append(event.getRequestId()).append(')');
        }
        return message.toString();
    }
}
