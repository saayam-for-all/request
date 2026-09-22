package org.sfa.request.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.repository.VolunteerMatchRepository;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.sfa.request.service.impl.VolunteerMatchServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Stage 2 of the notification flow: a notifiable request is routed to volunteers by
 * proximity when the help must be delivered in person, and by skill when it is remote.
 */
class VolunteerMatchRoutingTest {

    private static final String REQUESTER = "SID-REQUESTER";
    private static final double RADIUS_METERS = 25000;
    private static final int MAX_VOLUNTEERS = 25;

    private VolunteerMatchRepository repository;
    private VolunteerMatchServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(VolunteerMatchRepository.class);
        service = new VolunteerMatchServiceImpl(repository, RADIUS_METERS, MAX_VOLUNTEERS);
    }

    /**
     * A plain stub rather than a Mockito mock: these are built inline inside
     * {@code when(...).thenReturn(...)}, and nesting {@code when()} calls that way
     * trips Mockito's UnfinishedStubbing check.
     */
    private record StubVolunteer(String userId, String emailAddress, String phoneNumber)
            implements MatchedVolunteerRow {

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public String getEmailAddress() {
            return emailAddress;
        }

        @Override
        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    private static MatchedVolunteerRow volunteer(String id, String email) {
        return new StubVolunteer(id, email, null);
    }

    private static Request request(Integer typeId, String catId) {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId(REQUESTER);

        if (typeId != null) {
            RequestType type = new RequestType();
            type.setRequestTypeId(typeId);
            request.setRequestType(type);
        }
        if (catId != null) {
            HelpCategory category = new HelpCategory();
            category.setCatId(catId);
            request.setHelpCategory(category);
        }
        return request;
    }

    // ---------- routing ----------

    @Test
    void inPersonRequestRoutesByProximity() {
        when(repository.findVolunteersNearRequester(eq(REQUESTER), anyDouble(), anyInt()))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.IN_PERSON.getId(), "CAT-1"));

        verify(repository).findVolunteersNearRequester(REQUESTER, RADIUS_METERS, MAX_VOLUNTEERS);
        verify(repository, never()).findVolunteersByCategory(anyString(), anyString());
    }

    @Test
    void remoteRequestRoutesBySkill() {
        when(repository.findVolunteersByCategory(eq("CAT-1"), eq(REQUESTER)))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.REMOTE.getId(), "CAT-1"));

        verify(repository).findVolunteersByCategory("CAT-1", REQUESTER);
        verify(repository, never()).findVolunteersNearRequester(anyString(), anyDouble(), anyInt());
    }

    /** An in-person request must not fall back to skill matching when nobody is near. */
    @Test
    void inPersonRequestWithNoOneNearbyDoesNotFallBackToSkill() {
        when(repository.findVolunteersNearRequester(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of());

        List<NotificationRecipient> recipients = service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.IN_PERSON.getId(), "CAT-1"));

        assertTrue(recipients.isEmpty());
        verify(repository, never()).findVolunteersByCategory(anyString(), anyString());
    }

    @Test
    void unspecifiedTypeRoutesBySkill() {
        when(repository.findVolunteersByCategory(eq("CAT-1"), eq(REQUESTER)))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.UNSPECIFIED.getId(), "CAT-1"));

        verify(repository).findVolunteersByCategory("CAT-1", REQUESTER);
        verify(repository, never()).findVolunteersNearRequester(anyString(), anyDouble(), anyInt());
    }

    @Test
    void missingRequestTypeRoutesBySkill() {
        when(repository.findVolunteersByCategory(eq("CAT-1"), eq(REQUESTER)))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        service.resolveMatchedVolunteerRecipients(request(null, "CAT-1"));

        verify(repository).findVolunteersByCategory("CAT-1", REQUESTER);
        verify(repository, never()).findVolunteersNearRequester(anyString(), anyDouble(), anyInt());
    }

    /** The configured radius and cap are what reach the query, not hard-coded values. */
    @Test
    void proximitySearchUsesTheConfiguredRadiusAndCap() {
        VolunteerMatchServiceImpl tuned = new VolunteerMatchServiceImpl(repository, 500.0, 3);
        when(repository.findVolunteersNearRequester(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of());

        tuned.resolveMatchedVolunteerRecipients(request(RequestTypeEnum.IN_PERSON.getId(), "CAT-1"));

        ArgumentCaptor<Double> radius = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Integer> cap = ArgumentCaptor.forClass(Integer.class);
        verify(repository).findVolunteersNearRequester(eq(REQUESTER), radius.capture(), cap.capture());

        assertEquals(500.0, radius.getValue());
        assertEquals(3, cap.getValue());
    }

    // ---------- recipient expansion ----------

    @Test
    void eachVolunteerGetsInAppAndEmailRecipients() {
        when(repository.findVolunteersByCategory(anyString(), anyString()))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        List<NotificationRecipient> recipients = service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.REMOTE.getId(), "CAT-1"));

        assertEquals(2, recipients.size());
        assertEquals(NotificationChannel.IN_APP, recipients.get(0).getChannel());
        assertEquals(NotificationChannel.EMAIL, recipients.get(1).getChannel());
        assertEquals("vol1@example.org", recipients.get(1).getTargetAddress());
    }

    @Test
    void volunteerWithNoEmailIsStillReachableInApp() {
        when(repository.findVolunteersNearRequester(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(volunteer("VOL-1", null)));

        List<NotificationRecipient> recipients = service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.IN_PERSON.getId(), "CAT-1"));

        assertEquals(1, recipients.size());
        assertEquals(NotificationChannel.IN_APP, recipients.get(0).getChannel());
        assertEquals("VOL-1", recipients.get(0).getUserId());
    }

    @Test
    void remoteRequestWithNoHelpCategoryMatchesNobody() {
        List<NotificationRecipient> recipients = service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.REMOTE.getId(), null));

        assertTrue(recipients.isEmpty());
        verify(repository, never()).findVolunteersByCategory(anyString(), anyString());
    }

    /** Proximity matching does not need a help category; location is the criterion. */
    @Test
    void inPersonRequestWithNoHelpCategoryStillMatchesByProximity() {
        when(repository.findVolunteersNearRequester(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(volunteer("VOL-1", "vol1@example.org")));

        List<NotificationRecipient> recipients = service.resolveMatchedVolunteerRecipients(
                request(RequestTypeEnum.IN_PERSON.getId(), null));

        assertEquals(2, recipients.size());
    }
}
