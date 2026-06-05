package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.Test;
import org.sfa.request.config.ObjectMapperConfig;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.service.api.NotificationDispatchService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationQueueHandlerTest {

    private final NotificationDispatchService notificationDispatchService = mock(NotificationDispatchService.class);
    private final NotificationQueueHandler handler = new NotificationQueueHandler();

    @Test
    void handleRequestDispatchesValidRecords() throws Exception {
        ReflectionTestUtils.setField(handler, "notificationDispatchService", notificationDispatchService);
        NotificationEvent event = event();

        SQSBatchResponse response = handler.handleRequest(sqsEvent("msg-1", event), null);

        assertEquals(0, response.getBatchItemFailures().size());
        verify(notificationDispatchService).dispatch(event);
    }

    @Test
    void handleRequestReturnsBatchFailureForFailedRecord() throws Exception {
        ReflectionTestUtils.setField(handler, "notificationDispatchService", notificationDispatchService);
        NotificationEvent event = event();
        doThrow(new IllegalStateException("delivery failed"))
                .when(notificationDispatchService).dispatch(event);

        SQSBatchResponse response = handler.handleRequest(sqsEvent("msg-1", event), null);

        assertEquals(1, response.getBatchItemFailures().size());
        assertEquals("msg-1", response.getBatchItemFailures().get(0).getItemIdentifier());
    }

    private SQSEvent sqsEvent(String messageId, NotificationEvent event) throws Exception {
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId(messageId);
        message.setBody(ObjectMapperConfig.getObjectMapper().writeValueAsString(event));

        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(message));
        return sqsEvent;
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
