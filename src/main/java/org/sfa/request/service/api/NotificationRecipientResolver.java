package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;

import java.util.List;

public interface NotificationRecipientResolver {
    List<NotificationRecipient> resolveRequestRecipients(Request request);
}
