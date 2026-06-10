package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.model.entity.Request;

import java.util.Locale;

public interface NotificationEventService {
    NotificationEvent buildRequestEvent(NotificationEventType eventType, Request request, Locale locale);

    void enqueueRequestEvent(NotificationEventType eventType, Request request, Locale locale);
}
