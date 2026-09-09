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
import java.util.HashMap;
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
                .attributes(buildAttributes(request))
                .createdAt(ZonedDateTime.now())
                .build();
    }


    private Map<String, String> buildAttributes(Request request) {
        Map<String, String> attributes = new HashMap<>();
        // On this branch the lookup entities hold typed enums (@Enumerated(EnumType.STRING)),
        // so the attribute value is the enum constant name.
        if (request.getRequestStatus() != null && request.getRequestStatus().getStatus() != null) {
            attributes.put("requestStatus", request.getRequestStatus().getStatus().name());
        }
        if (request.getRequestPriority() != null && request.getRequestPriority().getPriority() != null) {
            attributes.put("requestPriority", request.getRequestPriority().getPriority().name());
        }
        if (request.getRequestType() != null && request.getRequestType().getType() != null) {
            attributes.put("requestType", request.getRequestType().getType().name());
        }
        if (request.getHelpCategory() != null) {
            attributes.put("helpCategoryId", request.getHelpCategory().getCatId());
            attributes.put("helpCategory", request.getHelpCategory().getCatName());
        }
        return attributes;
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
