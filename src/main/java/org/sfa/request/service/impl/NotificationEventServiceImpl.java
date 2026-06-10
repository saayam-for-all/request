package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.model.entity.Request;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.sfa.request.service.api.SQSService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventServiceImpl implements NotificationEventService {

    private final SQSService sqsService;
    private final NotificationRecipientResolver notificationRecipientResolver;

    @Override
    public NotificationEvent buildRequestEvent(NotificationEventType eventType, Request request, Locale locale) {
        return NotificationEvent.builder()
                .eventType(eventType)
                .requestId(request.getRequestId())
                .requesterId(request.getRequesterId())
                .recipients(notificationRecipientResolver.resolveRequestRecipients(request))
                .locale(locale)
                .subject(request.getRequestSubject())
                .body(request.getRequestDescription())
                .attributes(Map.of(
                        "requestStatus", request.getRequestStatus().getStatus(),
                        "requestPriority", request.getRequestPriority().getPriority(),
                        "requestType", request.getRequestType().getType()
                ))
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Override
    public void enqueueRequestEvent(NotificationEventType eventType, Request request, Locale locale) {
        NotificationEvent event = buildRequestEvent(eventType, request, locale);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendEvent(event);
                }
            });
            return;
        }

        sendEvent(event);
    }

    private void sendEvent(NotificationEvent event) {
        try {
            sqsService.sendMessage(event);
        } catch (RuntimeException e) {
            log.warn("Unable to enqueue notification event {} for request {}",
                    event.getEventType(),
                    event.getRequestId(),
                    e);
        }
    }
}
