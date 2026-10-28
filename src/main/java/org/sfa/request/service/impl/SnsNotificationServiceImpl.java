package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.SnsNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsNotificationServiceImpl implements SnsNotificationService {

    private final SnsClient snsClient;

    @Override
    public void sendSms(NotificationEvent event, NotificationRecipient recipient) {
        if (!StringUtils.hasText(recipient.getTargetAddress())) {
            throw new IllegalArgumentException("Missing SMS phone number for user " + recipient.getUserId());
        }

        snsClient.publish(PublishRequest.builder()
                .phoneNumber(recipient.getTargetAddress())
                .message(resolveMessage(event))
                .build());

        log.info("Sent {} SMS notification for request {} to user {}",
                event.getEventType(),
                event.getRequestId(),
                recipient.getUserId());
    }

    @Override
    public void sendPush(NotificationEvent event, NotificationRecipient recipient) {
        if (!StringUtils.hasText(recipient.getTargetAddress())) {
            throw new IllegalArgumentException("Missing SNS endpoint ARN for user " + recipient.getUserId());
        }

        snsClient.publish(PublishRequest.builder()
                .targetArn(recipient.getTargetAddress())
                .message(resolveMessage(event))
                .build());

        log.info("Sent {} push notification for request {} to user {}",
                event.getEventType(),
                event.getRequestId(),
                recipient.getUserId());
    }

    private String resolveMessage(NotificationEvent event) {
        return StringUtils.hasText(event.getBody())
                ? event.getBody()
                : "Your Saayam request has an update.";
    }
}
