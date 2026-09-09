package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.EmailNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final SesClient sesClient;

    @Value("${ses.sender.email}")
    private String senderEmail;

    @Override
    public void sendEmail(NotificationEvent event, NotificationRecipient recipient) {
        if (!StringUtils.hasText(senderEmail)) {
            throw new IllegalStateException("Missing SES sender email. Set AWS_SES_SENDER_EMAIL.");
        }
        if (!StringUtils.hasText(recipient.getTargetAddress())) {
            throw new IllegalArgumentException("Missing email target address for user " + recipient.getUserId());
        }

        SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder()
                        .toAddresses(recipient.getTargetAddress())
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .data(resolveSubject(event))
                                .charset("UTF-8")
                                .build())
                        .body(Body.builder()
                                .text(Content.builder()
                                        .data(resolveBody(event))
                                        .charset("UTF-8")
                                        .build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
        log.info("Sent {} email notification for request {} to user {}",
                event.getEventType(),
                event.getRequestId(),
                recipient.getUserId());
    }

    private String resolveSubject(NotificationEvent event) {
        return StringUtils.hasText(event.getSubject())
                ? event.getSubject()
                : "Saayam request notification";
    }

    private String resolveBody(NotificationEvent event) {
        return StringUtils.hasText(event.getBody())
                ? event.getBody()
                : "Your request has an update.";
    }
}
