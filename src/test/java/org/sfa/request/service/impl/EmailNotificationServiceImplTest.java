package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailNotificationServiceImplTest {

    private final SesClient sesClient = mock(SesClient.class);
    private final EmailNotificationServiceImpl service = new EmailNotificationServiceImpl(sesClient);

    @Test
    void sendEmailCallsSesClient() {
        ReflectionTestUtils.setField(service, "senderEmail", "sender@example.org");

        service.sendEmail(event(), new NotificationRecipient(
                "USER-1",
                NotificationChannel.EMAIL,
                "user@example.org"
        ));

        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmailRequiresSenderEmail() {
        assertThrows(IllegalStateException.class, () -> service.sendEmail(event(), new NotificationRecipient(
                "USER-1",
                NotificationChannel.EMAIL,
                "user@example.org"
        )));
    }

    private NotificationEvent event() {
        return NotificationEvent.builder()
                .eventType(NotificationEventType.REQUEST_CREATED)
                .requestId("REQ-1")
                .requesterId("USER-1")
                .subject("Request update")
                .body("Your request has an update")
                .build();
    }
}
