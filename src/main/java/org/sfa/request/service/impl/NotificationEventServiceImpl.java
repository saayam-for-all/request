package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.model.entity.Request;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.sfa.request.service.api.SQSService;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.SentimentAssessment;
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
    private final SentimentAnalysisService sentimentAnalysisService;

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
        if (request.getRequestStatus() != null) {
            attributes.put("requestStatus", request.getRequestStatus().getStatus());
        }
        if (request.getRequestPriority() != null) {
            attributes.put("requestPriority", request.getRequestPriority().getPriority());
        }
        if (request.getRequestType() != null) {
            attributes.put("requestType", request.getRequestType().getType());
        }
        if (request.getHelpCategory() != null) {
            attributes.put("helpCategoryId", request.getHelpCategory().getCatId());
            attributes.put("helpCategory", request.getHelpCategory().getCatName());
        }
        return attributes;
    }

    /**
     * Runs the notification flow for a request: sentiment analysis first, then
     * in-person/remote volunteer matching, then dispatch.
     *
     * <p>Two gates short-circuit before anything is enqueued, and both are logged
     * rather than failing silently (ba#40 FR-08):
     * <ol>
     *   <li>a request whose sentiment is not {@code GOOD_REQUEST} is never routed
     *       to volunteers, whatever the matching would have returned;</li>
     *   <li>a request that matches no volunteer produces no event, so downstream
     *       never sees an event with an empty recipient list.</li>
     * </ol>
     * Neither gate changes how the request itself is persisted.
     */
    @Override
    public void enqueueRequestEvent(NotificationEventType eventType, Request request, Locale locale) {
        SentimentAssessment sentiment = sentimentAnalysisService.assess(request);
        if (!sentiment.notifiable()) {
            // Log the classification only. The matched term is request-derived text
            // and is deliberately kept out of the logs (ba#40 NFR-02).
            log.warn("Suppressing {} notification for request {}: sentiment {} (code {})",
                    eventType, request.getRequestId(), sentiment.code(), sentiment.code().getCode());
            return;
        }

        NotificationEvent event = buildRequestEvent(eventType, request, locale);

        if (event.getRecipients() == null || event.getRecipients().isEmpty()) {
            log.info("No volunteer matched request {}; no {} notification enqueued",
                    request.getRequestId(), eventType);
            return;
        }

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
