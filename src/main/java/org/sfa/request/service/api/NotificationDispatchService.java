package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationEvent;

public interface NotificationDispatchService {
    void dispatch(NotificationEvent event);
}
