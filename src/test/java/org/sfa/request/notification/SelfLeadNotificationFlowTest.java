package org.sfa.request.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.SentimentCode;
import org.sfa.request.repository.VolunteerMatchRepository;
import org.sfa.request.service.api.SQSService;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.SentimentAssessment;
import org.sfa.request.service.impl.NotificationEventServiceImpl;
import org.sfa.request.service.impl.NotificationRecipientResolverImpl;
import org.sfa.request.service.impl.VolunteerMatchServiceImpl;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Issue #94: a self-led request runs through the real notification flow (sentiment
 * check, then volunteer matching). The sentiment check still runs, no volunteer is
 * searched for, and nothing is sent. Only the database and the queue are fakes.
 */
class SelfLeadNotificationFlowTest {

    private SQSService sqsService;
    private SentimentAnalysisService sentimentAnalysisService;
    private VolunteerMatchRepository volunteerMatchRepository;
    private NotificationEventServiceImpl notificationEventService;

    @BeforeEach
    void setUp() {
        sqsService = mock(SQSService.class);
        sentimentAnalysisService = mock(SentimentAnalysisService.class);
        volunteerMatchRepository = mock(VolunteerMatchRepository.class);

        VolunteerMatchServiceImpl matching =
                new VolunteerMatchServiceImpl(volunteerMatchRepository, 25000, 25, 3218.69, 8046.72);
        notificationEventService = new NotificationEventServiceImpl(
                sqsService, new NotificationRecipientResolverImpl(matching), sentimentAnalysisService);
    }

    @Test
    void cleanSelfLedRequestIsCheckedButNotSent() {
        when(sentimentAnalysisService.assess(any())).thenReturn(SentimentAssessment.good());

        notificationEventService.enqueueRequestEvent(
                NotificationEventType.REQUEST_CREATED, selfLedInPersonRequest(), Locale.US);

        verify(sentimentAnalysisService).assess(any());
        verifyNoInteractions(volunteerMatchRepository);
        verifyNoInteractions(sqsService);
    }

    @Test
    void threateningSelfLedRequestIsStillCaughtBySentimentCheck() {
        when(sentimentAnalysisService.assess(any()))
                .thenReturn(new SentimentAssessment(SentimentCode.THREATENING, "term"));

        notificationEventService.enqueueRequestEvent(
                NotificationEventType.REQUEST_CREATED, selfLedInPersonRequest(), Locale.US);

        verify(sentimentAnalysisService).assess(any());
        verifyNoInteractions(volunteerMatchRepository);
        verifyNoInteractions(sqsService);
    }

    private static Request selfLedInPersonRequest() {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId("SID-1");
        request.setRequestSubject("Flooded basement");
        request.setRequestDescription("I will organise the cleanup");
        request.setIsCalamity(true);

        RequestIsLeadVolunteer lead = new RequestIsLeadVolunteer();
        lead.setReqIslead("YES");
        request.setIsLeadVolunteer(lead);

        RequestType type = new RequestType();
        type.setType("IN_PERSON");
        request.setRequestType(type);
        return request;
    }
}
