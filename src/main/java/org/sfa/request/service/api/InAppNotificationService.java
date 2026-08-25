package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;

public interface InAppNotificationService {

    /**
     * Persists an in-app notification for the recipient. The Volunteer microservice's
     * notification read APIs surface the row to the client.
     */
    void record(NotificationEvent event, NotificationRecipient recipient);
}
