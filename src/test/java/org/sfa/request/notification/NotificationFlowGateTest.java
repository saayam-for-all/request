package org.sfa.request.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.SentimentCode;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.sfa.request.service.api.SQSService;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.SentimentAssessment;
import org.sfa.request.service.impl.NotificationEventServiceImpl;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The order of the end-to-end flow: sentiment analysis, then volunteer matching,
 * then dispatch. Each gate short-circuits everything downstream of it.
 */
class NotificationFlowGateTest {

    private SQSService sqsService;
    private NotificationRecipientResolver recipientResolver;
    private SentimentAnalysisService sentimentAnalysisService;
    private NotificationEventServiceImpl service;

    @BeforeEach
    void setUp() {
        sqsService = mock(SQSService.class);
        recipientResolver = mock(NotificationRecipientResolver.class);
        sentimentAnalysisService = mock(SentimentAnalysisService.class);
        service = new NotificationEventServiceImpl(
                sqsService, recipientResolver, sentimentAnalysisService);
    }

    private static Request request() {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId("SID-REQUESTER");
        request.setRequestSubject("Need a ride");
        request.setRequestDescription("Lift to the clinic");
        return request;
    }

    private static List<NotificationRecipient> oneRecipient() {
        return List.of(new NotificationRecipient("VOL-1", NotificationChannel.IN_APP, null));
    }

    @Test
    void cleanRequestWithMatchesIsEnqueued() {
        when(sentimentAnalysisService.assess(any())).thenReturn(SentimentAssessment.good());
        when(recipientResolver.resolveRequestRecipients(any())).thenReturn(oneRecipient());

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verify(sqsService).sendMessage(any(NotificationEvent.class));
    }

    /**
     * The sentiment gate runs first: a flagged request must not even be matched,
     * so no volunteer is looked up and nothing is dispatched.
     */
    @Test
    void foulRequestIsNeitherMatchedNorEnqueued() {
        when(sentimentAnalysisService.assess(any()))
                .thenReturn(new SentimentAssessment(SentimentCode.FOUL_LANGUAGE, "idiot"));

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verifyNoInteractions(recipientResolver);
        verifyNoInteractions(sqsService);
    }

    @Test
    void suicidalRequestIsNeitherMatchedNorEnqueued() {
        when(sentimentAnalysisService.assess(any()))
                .thenReturn(new SentimentAssessment(SentimentCode.DEPRESSIVE_OR_SUICIDAL, "suicidal"));

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verifyNoInteractions(recipientResolver);
        verifyNoInteractions(sqsService);
    }

    @Test
    void threateningRequestIsNeitherMatchedNorEnqueued() {
        when(sentimentAnalysisService.assess(any()))
                .thenReturn(new SentimentAssessment(SentimentCode.THREATENING, "gun"));

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verifyNoInteractions(recipientResolver);
        verifyNoInteractions(sqsService);
    }

    /**
     * ba#40 FR-08: a no-match is a recorded outcome, not a dispatched event with an
     * empty recipient list.
     */
    @Test
    void cleanRequestWithNoMatchesIsNotEnqueued() {
        when(sentimentAnalysisService.assess(any())).thenReturn(SentimentAssessment.good());
        when(recipientResolver.resolveRequestRecipients(any())).thenReturn(List.of());

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verify(recipientResolver).resolveRequestRecipients(any());
        verify(sqsService, never()).sendMessage(any());
    }

    @Test
    void nullRecipientListIsTreatedAsNoMatch() {
        when(sentimentAnalysisService.assess(any())).thenReturn(SentimentAssessment.good());
        when(recipientResolver.resolveRequestRecipients(any())).thenReturn(null);

        service.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        verify(sqsService, never()).sendMessage(any());
    }

    /** buildRequestEvent still carries the matched recipients onto the event. */
    @Test
    void builtEventCarriesTheMatchedRecipients() {
        when(recipientResolver.resolveRequestRecipients(any())).thenReturn(oneRecipient());

        NotificationEvent event = service.buildRequestEvent(
                NotificationEventType.REQUEST_CREATED, request(), Locale.US);

        assertEquals(1, event.getRecipients().size());
        assertEquals("VOL-1", event.getRecipients().get(0).getUserId());
        assertEquals("REQ-1", event.getRequestId());
    }
}
