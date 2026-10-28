package org.sfa.request.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.sfa.request.config.ObjectMapperConfig;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.service.api.SQSService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SQSServiceImpl implements SQSService {

    private final SqsTemplate sqsTemplate;
    private final String sqsUrl;

    public SQSServiceImpl(SqsTemplate sqsTemplate, @Value("${sqs.url}") String sqsUrl) {
        this.sqsTemplate = sqsTemplate;
        this.sqsUrl = sqsUrl;
    }

    @Override
    public void sendMessage(NotificationEvent event) {
        if (!StringUtils.hasText(sqsUrl)) {
            throw new IllegalStateException("Missing SQS queue URL. Set AWS_SQS_NOTIFICATION_QUEUE_URL.");
        }

        try {
            String jsonMessage = ObjectMapperConfig.getObjectMapper().writeValueAsString(event);
            sqsTemplate.send(sqsUrl, jsonMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting notification event to JSON", e);
        }
    }
}
