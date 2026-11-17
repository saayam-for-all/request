package org.sfa.request.stepfunctions;

import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.SQSService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.sfa.request.stepfunctions.Part4RatingStore.Purpose;
import static org.sfa.request.stepfunctions.WorkflowFinalizationTask.validId;

/** Transport for a committed invitation intent claimed by the callback service's outbox relay. */
@Component
public class RatingInvitationPublisher {
    private final SQSService sqs;

    public RatingInvitationPublisher(SQSService sqs) {
        this.sqs = sqs;
    }

    public void publish(String invitationId, String requestId, Purpose purpose,
                        String participantId, NotificationRecipient recipient) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Rating invitations must be published after commit");
        }
        if (!validId(invitationId) || !validId(requestId) || !validId(participantId) || purpose == null
                || recipient == null || !participantId.equals(recipient.getUserId())
                || recipient.getChannel() == null || recipient.getChannel() == NotificationChannel.IN_APP
                || recipient.getTargetAddress() == null || recipient.getTargetAddress().isBlank()) {
            throw new IllegalArgumentException("Invalid rating invitation recipient");
        }
        sqs.sendMessage(NotificationEvent.builder()
                .eventType(NotificationEventType.RATING_REQUESTED)
                .requestId(requestId)
                .recipients(List.of(recipient))
                .locale(Locale.ROOT)
                .subject("Please rate your Saayam experience")
                .body("Open Saayam to rate your experience with your resolved help request.")
                .attributes(Map.of("invitationId", invitationId, "purpose", purpose.name()))
                .createdAt(ZonedDateTime.now())
                .build());
    }
}
