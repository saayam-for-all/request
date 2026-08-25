package org.sfa.request.service.api;

import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;

import java.util.List;

public interface VolunteerMatchService {

    /**
     * Resolves the volunteers who should be notified about a help request, expanded
     * into one recipient per volunteer per enabled channel.
     */
    List<NotificationRecipient> resolveMatchedVolunteerRecipients(Request request);
}
