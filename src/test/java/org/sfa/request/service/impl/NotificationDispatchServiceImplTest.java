package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.EmailNotificationService;
import org.sfa.request.service.api.SnsNotificationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationDispatchServiceImplTest {

    private final EmailNotificationService emailNotificationService = mock(EmailNotificationService.class);
    private final SnsNotificationService snsNotificationService = mock(SnsNotificationService.class);
    private final NotificationDispatchServiceImpl service =
            new NotificationDispatchServiceImpl(emailNotificationService, snsNotificationService);

    @Test
    void dispatchRoutesRecipientsByChannel() {
        NotificationEvent event = event(
                new NotificationRecipient("USER-1", NotificationChannel.EMAIL, "user@example.org"),
                new NotificationRecipient("USER-1", NotificationChannel.SMS, "+15555550100"),
                new NotificationRecipient("USER-1", NotificationChannel.PUSH, "arn:aws:sns:endpoint")
        );

        service.dispatch(event);

        verify(emailNotificationService).sendEmail(event, event.getRecipients().get(0));
        verify(snsNotificationService).sendSms(event, event.getRecipients().get(1));
        verify(snsNotificationService).sendPush(event, event.getRecipients().get(2));
    }

    @Test
    void dispatchThrowsWhenAnyRecipientDeliveryFails() {
        NotificationEvent event =
                event(new NotificationRecipient("USER-1", NotificationChannel.EMAIL, "user@example.org"));
        doThrow(new IllegalStateException("SES unavailable"))
                .when(emailNotificationService).sendEmail(event, event.getRecipients().get(0));

        assertThrows(IllegalStateException.class, () -> service.dispatch(event));
    }

    private NotificationEvent event(NotificationRecipient... recipients) {
        return NotificationEvent.builder()
                .eventType(NotificationEventType.REQUEST_CREATED)
                .requestId("REQ-1")
                .requesterId("USER-1")
                .recipients(List.of(recipients))
                .subject("Request update")
                .body("Your request has an update")
                .build();
    }
}
