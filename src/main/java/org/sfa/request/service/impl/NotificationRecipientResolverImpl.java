package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.sfa.request.service.api.VolunteerMatchService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Resolves who is notified about a help request.
 *
 * <p>Replaces PR #66's resolver, which addressed the <em>requester</em> (and produced a
 * null target address for authenticated users). The delivered behaviour notifies the
 * <em>matched volunteers</em>, per MVP requirement "Volunteers receive matching request
 * alerts via email, SMS, and dashboard notifications".
 */
@Service
@RequiredArgsConstructor
public class NotificationRecipientResolverImpl implements NotificationRecipientResolver {

    private final VolunteerMatchService volunteerMatchService;

    @Override
    public List<NotificationRecipient> resolveRequestRecipients(Request request) {
        return volunteerMatchService.resolveMatchedVolunteerRecipients(request);
    }
}
