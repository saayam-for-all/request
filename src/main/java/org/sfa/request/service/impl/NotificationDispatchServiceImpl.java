package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.EmailNotificationService;
import org.sfa.request.service.api.NotificationDispatchService;
import org.sfa.request.service.api.SnsNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final EmailNotificationService emailNotificationService;
    private final SnsNotificationService snsNotificationService;

    @Override
    public void dispatch(NotificationEvent event) {
        if (event == null || CollectionUtils.isEmpty(event.getRecipients())) {
            log.warn("Skipping notification dispatch because event or recipients are missing");
            return;
        }

        List<RuntimeException> failures = new ArrayList<>();
        event.getRecipients().forEach(recipient -> {
            try {
                route(event, recipient);
            } catch (RuntimeException e) {
                failures.add(e);
                log.warn("Notification delivery failed for request {} user {} channel {}",
                        event.getRequestId(),
                        recipient.getUserId(),
                        recipient.getChannel(),
                        e);
            }
        });

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Notification dispatch failed for one or more recipients");
        }
    }

    private void route(NotificationEvent event, NotificationRecipient recipient) {
        switch (recipient.getChannel()) {
            case EMAIL -> routeEmail(event, recipient);
            case SMS -> routeSms(event, recipient);
            case PUSH -> routePush(event, recipient);
            default -> log.warn("Unsupported notification channel {} for request {}",
                    recipient.getChannel(),
                    event.getRequestId());
        }
    }

    private void routeEmail(NotificationEvent event, NotificationRecipient recipient) {
        emailNotificationService.sendEmail(event, recipient);
    }

    private void routeSms(NotificationEvent event, NotificationRecipient recipient) {
        snsNotificationService.sendSms(event, recipient);
    }

    private void routePush(NotificationEvent event, NotificationRecipient recipient) {
        snsNotificationService.sendPush(event, recipient);
    }
}
