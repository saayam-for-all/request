package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;

public interface EmailNotificationService {
    void sendEmail(NotificationEvent event, NotificationRecipient recipient);
}
