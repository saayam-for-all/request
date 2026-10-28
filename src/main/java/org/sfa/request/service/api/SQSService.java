package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;

public interface SQSService {
    void sendMessage(NotificationEvent event);
}
