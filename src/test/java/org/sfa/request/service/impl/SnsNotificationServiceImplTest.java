package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SnsNotificationServiceImplTest {

    private final SnsClient snsClient = mock(SnsClient.class);
    private final SnsNotificationServiceImpl service = new SnsNotificationServiceImpl(snsClient);

    @Test
    void sendSmsPublishesToPhoneNumber() {
        service.sendSms(event(), new NotificationRecipient(
                "USER-1",
                NotificationChannel.SMS,
                "+15555550100"
        ));

        verify(snsClient).publish(any(PublishRequest.class));
    }

    @Test
    void sendPushPublishesToTargetArn() {
        service.sendPush(event(), new NotificationRecipient(
                "USER-1",
                NotificationChannel.PUSH,
                "arn:aws:sns:us-east-1:123456789012:endpoint/APNS/app/device"
        ));

        verify(snsClient).publish(any(PublishRequest.class));
    }

    @Test
    void sendSmsRequiresPhoneNumber() {
        assertThrows(IllegalArgumentException.class, () -> service.sendSms(event(), new NotificationRecipient(
                "USER-1",
                NotificationChannel.SMS,
                null
        )));
    }

    private NotificationEvent event() {
        return NotificationEvent.builder()
                .eventType(NotificationEventType.REQUEST_CREATED)
                .requestId("REQ-1")
                .requesterId("USER-1")
                .body("Your request has an update")
                .build();
    }
}
