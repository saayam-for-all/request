package org.sfa.request.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.repository.VolunteerMatchRepository;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.sfa.request.service.impl.VolunteerMatchServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Issue #94: the calamity ring search and the self-lead skip, plus how they combine
 * with in-person and remote requests.
 */
class CalamityAndSelfLeadMatchingTest {

    private static final String REQUEST_ID = "REQ-1";
    private static final String REQUESTER = "SID-REQUESTER";
    private static final double RADIUS_METERS = 25000;
    private static final int MAX_VOLUNTEERS = 25;
    private static final double TWO_MILES = 3218.69;
    private static final double FIVE_MILES = 8046.72;

    private VolunteerMatchRepository repository;
    private VolunteerMatchServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(VolunteerMatchRepository.class);
        service = new VolunteerMatchServiceImpl(repository, RADIUS_METERS, MAX_VOLUNTEERS, TWO_MILES, FIVE_MILES);
    }

    // ---------- calamity ----------

    @Test
    void inPersonCalamitySearchesTheRingAroundTheHelpLocation() {
        when(repository.findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new StubVolunteer("VOL-1", "vol1@example.org")));

        List<NotificationRecipient> recipients =
                service.resolveMatchedVolunteerRecipients(request("NO", true, "IN_PERSON", "SELF"));

        assertEquals(2, recipients.size());  // in-app + email for the one volunteer
        verify(repository).findVolunteersInRingAroundRequest(
                REQUEST_ID, REQUESTER, true, TWO_MILES, FIVE_MILES, MAX_VOLUNTEERS);
        verify(repository, never()).findVolunteersNearRequester(anyString(), anyDouble(), anyInt());
        verify(repository, never()).findVolunteersByCategory(anyString(), anyString());
    }

    /** For someone else's request, the requester's own location is not a valid centre. */
    @Test
    void calamityForSomeoneElseDoesNotFallBackToTheRequestersLocation() {
        when(repository.findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of());

        service.resolveMatchedVolunteerRecipients(request("NO", true, "IN_PERSON", "OTHER"));

        verify(repository).findVolunteersInRingAroundRequest(
                REQUEST_ID, REQUESTER, false, TWO_MILES, FIVE_MILES, MAX_VOLUNTEERS);
    }

    /** Nobody in the ring means no match. No wider search; the request goes to VOLUNTEER_NOT_FOUND. */
    @Test
    void emptyCalamityRingDoesNotWidenOrFallBack() {
        when(repository.findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of());

        List<NotificationRecipient> recipients =
                service.resolveMatchedVolunteerRecipients(request("NO", true, "IN_PERSON", "SELF"));

        assertTrue(recipients.isEmpty());
        verify(repository, never()).findVolunteersNearRequester(anyString(), anyDouble(), anyInt());
        verify(repository, never()).findVolunteersByCategory(anyString(), anyString());
    }

    @Test
    void inPersonWithoutCalamityStillUsesTheNormalNearbySearch() {
        when(repository.findVolunteersNearRequester(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        service.resolveMatchedVolunteerRecipients(request("NO", false, "IN_PERSON", "SELF"));

        verify(repository).findVolunteersNearRequester(REQUESTER, RADIUS_METERS, MAX_VOLUNTEERS);
        verify(repository, never()).findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void remoteCalamityUsesTheSkillSearch() {
        when(repository.findVolunteersByCategory(anyString(), anyString())).thenReturn(List.of());

        service.resolveMatchedVolunteerRecipients(request("NO", true, "REMOTE", "SELF"));

        verify(repository).findVolunteersByCategory("CAT-1", REQUESTER);
        verify(repository, never()).findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void customRadiiReachTheQuery() {
        VolunteerMatchServiceImpl tuned = new VolunteerMatchServiceImpl(repository, RADIUS_METERS, 5, 1000, 4000);
        when(repository.findVolunteersInRingAroundRequest(anyString(), anyString(), anyBoolean(),
                anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of());

        tuned.resolveMatchedVolunteerRecipients(request("NO", true, "IN_PERSON", "SELF"));

        verify(repository).findVolunteersInRingAroundRequest(REQUEST_ID, REQUESTER, true, 1000, 4000, 5);
    }

    @Test
    void outerRadiusMustBeBiggerThanTheExclusionZone() {
        assertThrows(IllegalArgumentException.class,
                () -> new VolunteerMatchServiceImpl(repository, RADIUS_METERS, MAX_VOLUNTEERS, 5000, 5000));
        assertThrows(IllegalArgumentException.class,
                () -> new VolunteerMatchServiceImpl(repository, RADIUS_METERS, MAX_VOLUNTEERS, -1, 5000));
    }

    // ---------- self-lead ----------

    @Test
    void selfLeadRequestsMatchNobody() {
        for (String type : new String[]{"IN_PERSON", "REMOTE"}) {
            for (Boolean calamity : new Boolean[]{true, false}) {
                List<NotificationRecipient> recipients =
                        service.resolveMatchedVolunteerRecipients(request("YES", calamity, type, "SELF"));
                assertTrue(recipients.isEmpty(), "self-lead " + type + " calamity=" + calamity);
            }
        }
        verifyNoInteractions(repository);
    }

    // ---------- helpers ----------

    private static Request request(String lead, Boolean calamity, String type, String requestFor) {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER);
        request.setIsCalamity(calamity);

        RequestIsLeadVolunteer leadRow = new RequestIsLeadVolunteer();
        leadRow.setReqIslead(lead);
        request.setIsLeadVolunteer(leadRow);

        RequestType typeRow = new RequestType();
        typeRow.setType(type);
        request.setRequestType(typeRow);

        RequestFor forRow = new RequestFor();
        forRow.setRequestFor(requestFor);
        request.setRequestFor(forRow);

        HelpCategory category = new HelpCategory();
        category.setCatId("CAT-1");
        request.setHelpCategory(category);
        return request;
    }

    private record StubVolunteer(String userId, String emailAddress) implements MatchedVolunteerRow {
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
            return null;
        }
    }
}
