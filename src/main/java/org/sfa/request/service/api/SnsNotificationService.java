package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;

public interface SnsNotificationService {
    void sendSms(NotificationEvent event, NotificationRecipient recipient);

    void sendPush(NotificationEvent event, NotificationRecipient recipient);
}
