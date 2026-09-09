package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.config.ObjectMapperConfig;
import org.sfa.request.config.SpringContext;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.service.api.NotificationDispatchService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NotificationQueueHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
    private NotificationDispatchService notificationDispatchService;

    @Override
    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context lambdaContext) {
        if (sqsEvent == null || sqsEvent.getRecords() == null) {
            log.warn("Received empty SQS event");
            return new SQSBatchResponse();
        }

        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
        sqsEvent.getRecords().forEach(record -> {
            if (!dispatchRecord(record)) {
                failures.add(new SQSBatchResponse.BatchItemFailure(record.getMessageId()));
            }
        });

        return new SQSBatchResponse(failures);
    }

    private boolean dispatchRecord(SQSEvent.SQSMessage record) {
        try {
            NotificationEvent event = objectMapper.readValue(record.getBody(), NotificationEvent.class);
            getNotificationDispatchService().dispatch(event);
            return true;
        } catch (Exception e) {
            log.error("Unable to process notification SQS message {}", record.getMessageId(), e);
            return false;
        }
    }

    private NotificationDispatchService getNotificationDispatchService() {
        if (notificationDispatchService == null) {
            notificationDispatchService = SpringContext.getContext().getBean(NotificationDispatchService.class);
        }

        return notificationDispatchService;
    }
}
