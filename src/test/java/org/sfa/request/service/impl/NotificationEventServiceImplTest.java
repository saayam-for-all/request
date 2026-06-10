package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.sfa.request.service.api.SQSService;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationEventServiceImplTest {

    private final SQSService sqsService = mock(SQSService.class);
    private final NotificationRecipientResolver notificationRecipientResolver = mock(NotificationRecipientResolver.class);
    private final NotificationEventServiceImpl service =
            new NotificationEventServiceImpl(sqsService, notificationRecipientResolver);

    @Test
    void buildRequestEventIncludesRequestDetailsAndResolvedRecipients() {
        Request request = request();
        NotificationRecipient recipient =
                new NotificationRecipient("USER-1", NotificationChannel.EMAIL, "user@example.org");
        when(notificationRecipientResolver.resolveRequestRecipients(request)).thenReturn(List.of(recipient));

        NotificationEvent event = service.buildRequestEvent(
                NotificationEventType.REQUEST_CREATED,
                request,
                Locale.US
        );

        assertEquals(NotificationEventType.REQUEST_CREATED, event.getEventType());
        assertEquals("REQ-1", event.getRequestId());
        assertEquals("USER-1", event.getRequesterId());
        assertEquals(List.of(recipient), event.getRecipients());
        assertEquals("Need groceries", event.getSubject());
        assertEquals("Please help with grocery pickup", event.getBody());
        assertEquals("CREATED", event.getAttributes().get("requestStatus"));
        assertEquals("LOW", event.getAttributes().get("requestPriority"));
        assertEquals("IN_PERSON", event.getAttributes().get("requestType"));
    }

    @Test
    void enqueueRequestEventSendsBuiltEventToSqsOutsideTransaction() {
        Request request = request();
        when(notificationRecipientResolver.resolveRequestRecipients(request))
                .thenReturn(List.of(new NotificationRecipient("USER-1", NotificationChannel.EMAIL, "user@example.org")));

        service.enqueueRequestEvent(NotificationEventType.REQUEST_UPDATED, request, Locale.US);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(sqsService).sendMessage(captor.capture());
        assertEquals(NotificationEventType.REQUEST_UPDATED, captor.getValue().getEventType());
        assertEquals("REQ-1", captor.getValue().getRequestId());
    }

    private Request request() {
        RequestStatus status = new RequestStatus();
        status.setStatus("CREATED");

        RequestPriority priority = new RequestPriority();
        priority.setPriority("LOW");

        RequestType type = new RequestType();
        type.setType("IN_PERSON");

        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId("USER-1");
        request.setRequestSubject("Need groceries");
        request.setRequestDescription("Please help with grocery pickup");
        request.setRequestStatus(status);
        request.setRequestPriority(priority);
        request.setRequestType(type);
        return request;
    }
}
