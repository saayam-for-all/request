package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sfa.request.dto.OtpIssueRequestDTO;
import org.sfa.request.dto.OtpIssueResultDTO;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.exception.types.OtpDeliveryFailedException;
import org.sfa.request.exception.types.OtpRecipientUnreachableException;
import org.sfa.request.model.entity.OtpVerification;
import org.sfa.request.model.enums.OtpParty;
import org.sfa.request.model.enums.OtpVerificationState;
import org.sfa.request.repository.OtpVerificationRepository;
import org.sfa.request.repository.UserContactRepository;
import org.sfa.request.repository.projection.UserContactRow;
import org.sfa.request.service.api.EmailNotificationService;
import org.sfa.request.service.api.SnsNotificationService;
import org.sfa.request.service.api.WorkflowCallbackService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OtpIssueServiceImplTest {

    private static final String REQUEST_ID = "REQ-1";
    private static final String REQUESTER_ID = "SID-REQUESTER";
    private static final String LEAD_ID = "SID-LEAD";
    private static final String TOKEN = "token-new";
    private static final String REQUESTER_CODE = "111111";
    private static final String VOLUNTEER_CODE = "222222";
    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");
    private static final int VALIDITY_MINUTES = 60;
    private static final int MAX_ATTEMPTS = 3;

    private final OtpVerificationRepository otpRepository = mock(OtpVerificationRepository.class);
    private final UserContactRepository contactRepository = mock(UserContactRepository.class);
    private final SnsNotificationService sns = mock(SnsNotificationService.class);
    private final EmailNotificationService email = mock(EmailNotificationService.class);
    private final WorkflowCallbackService callback = mock(WorkflowCallbackService.class);
    private final OtpCodeHasher hasher = new OtpCodeHasher("test-secret");

    private OtpIssueServiceImpl service(int validityMinutes, int maxAttempts) {
        OtpCodeGenerator generator = new OtpCodeGenerator(sequence(111111, 222222));
        return new OtpIssueServiceImpl(otpRepository, contactRepository, generator, hasher,
                sns, email, callback, validityMinutes, maxAttempts, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private final OtpIssueServiceImpl service = service(VALIDITY_MINUTES, MAX_ATTEMPTS);

    @Test
    void storesHashesNotCodesAndSendsEachPartyTheirOwnCode() {
        givenContact(REQUESTER_ID, "+15550000001", "requester@example.org");
        givenContact(LEAD_ID, "+15550000002", "lead@example.org");
        givenNoPendingRecords();

        OtpIssueResultDTO result = service.issueAndSend(request());

        OtpVerification saved = savedRecord();
        assertEquals(REQUEST_ID, saved.getRequestId());
        assertEquals(REQUESTER_ID, saved.getRequesterId());
        assertEquals(LEAD_ID, saved.getLeadVolunteerId());
        assertEquals(OtpVerificationState.PENDING.name(), saved.getState());
        assertEquals(TOKEN, saved.getTaskToken());
        assertEquals(MAX_ATTEMPTS, saved.getRequesterCodeAttemptsRemaining());
        assertEquals(MAX_ATTEMPTS, saved.getVolunteerCodeAttemptsRemaining());
        assertFalse(saved.isRequesterCodeVerified());
        assertFalse(saved.isVolunteerCodeVerified());
        assertEquals(ZonedDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(VALIDITY_MINUTES), saved.getExpiresAt());

        // Only hashes are stored, and each verifies against the right code and owner.
        assertNotEquals(REQUESTER_CODE, saved.getRequesterCodeHash());
        assertNotEquals(VOLUNTEER_CODE, saved.getVolunteerCodeHash());
        String id = saved.getOtpVerificationId();
        assertTrue(hasher.matches(id, OtpParty.REQUESTER, REQUESTER_CODE, saved.getRequesterCodeHash()));
        assertTrue(hasher.matches(id, OtpParty.LEAD_VOLUNTEER, VOLUNTEER_CODE, saved.getVolunteerCodeHash()));
        assertFalse(hasher.matches(id, OtpParty.REQUESTER, VOLUNTEER_CODE, saved.getRequesterCodeHash()));

        // The requester receives only their code; the volunteer receives only theirs.
        assertSent(sns, REQUESTER_ID, NotificationChannel.SMS, "+15550000001", REQUESTER_CODE, VOLUNTEER_CODE);
        assertSent(sns, LEAD_ID, NotificationChannel.SMS, "+15550000002", VOLUNTEER_CODE, REQUESTER_CODE);
        assertSent(email, REQUESTER_ID, NotificationChannel.EMAIL, "requester@example.org", REQUESTER_CODE, VOLUNTEER_CODE);
        assertSent(email, LEAD_ID, NotificationChannel.EMAIL, "lead@example.org", VOLUNTEER_CODE, REQUESTER_CODE);

        assertEquals(id, result.getOtpVerificationId());
        assertEquals(REQUEST_ID, result.getRequestId());
        assertEquals(saved.getExpiresAt(), result.getExpiresAt());
        assertEquals(0, result.getSupersededCount());
        verifyNoInteractions(callback);
    }

    @Test
    void supersedesEarlierPendingRecordAndResumesItsExecution() {
        givenContact(REQUESTER_ID, "+15550000001", null);
        givenContact(LEAD_ID, "+15550000002", null);
        OtpVerification old = pendingRecord("token-old");
        when(otpRepository.findByRequestIdAndState(REQUEST_ID, "PENDING")).thenReturn(List.of(old));

        OtpIssueResultDTO result = service.issueAndSend(request());

        assertEquals(OtpVerificationState.SUPERSEDED.name(), old.getState());
        verify(otpRepository).saveAll(List.of(old));
        verify(callback).sendSuccess("token-old", Map.of("result", "SUPERSEDED"));
        assertEquals(1, result.getSupersededCount());
        assertEquals(OtpVerificationState.PENDING.name(), savedRecord().getState());
    }

    @Test
    void retryOfTheSameStepSupersedesWithoutResumingItself() {
        givenContact(REQUESTER_ID, "+15550000001", null);
        givenContact(LEAD_ID, "+15550000002", null);
        OtpVerification fromFailedAttempt = pendingRecord(TOKEN);
        when(otpRepository.findByRequestIdAndState(REQUEST_ID, "PENDING")).thenReturn(List.of(fromFailedAttempt));

        OtpIssueResultDTO result = service.issueAndSend(request());

        assertEquals(OtpVerificationState.SUPERSEDED.name(), fromFailedAttempt.getState());
        assertEquals(1, result.getSupersededCount());
        verifyNoInteractions(callback);
    }

    @Test
    void failingToResumeAnOldExecutionDoesNotBlockNewCodes() {
        givenContact(REQUESTER_ID, "+15550000001", null);
        givenContact(LEAD_ID, "+15550000002", null);
        when(otpRepository.findByRequestIdAndState(REQUEST_ID, "PENDING")).thenReturn(List.of(pendingRecord("token-old")));
        when(callback.sendSuccess(anyString(), anyMap())).thenThrow(new IllegalStateException("throttled"));

        service.issueAndSend(request());

        verify(sns, times(2)).sendSms(any(), any());
    }

    @Test
    void unreachableRequesterFailsBeforeAnythingChanges() {
        when(contactRepository.findContactByUserId(REQUESTER_ID)).thenReturn(Optional.empty());
        givenContact(LEAD_ID, "+15550000002", null);

        OtpRecipientUnreachableException e =
                assertThrows(OtpRecipientUnreachableException.class, () -> service.issueAndSend(request()));

        assertEquals(OtpParty.REQUESTER, e.getParty());
        assertEquals(REQUESTER_ID, e.getUserId());
        verifyNoInteractions(otpRepository, sns, email, callback);
    }

    @Test
    void volunteerWithNoPhoneOrEmailIsUnreachable() {
        givenContact(REQUESTER_ID, "+15550000001", null);
        givenContact(LEAD_ID, " ", null);

        OtpRecipientUnreachableException e =
                assertThrows(OtpRecipientUnreachableException.class, () -> service.issueAndSend(request()));

        assertEquals(OtpParty.LEAD_VOLUNTEER, e.getParty());
        verifyNoInteractions(otpRepository, sns, email, callback);
    }

    @Test
    void usesOnlyTheChannelsEachPartyHas() {
        givenContact(REQUESTER_ID, "+15550000001", null);
        givenContact(LEAD_ID, null, "lead@example.org");
        givenNoPendingRecords();

        service.issueAndSend(request());

        verify(sns).sendSms(any(), argThat(r -> REQUESTER_ID.equals(r.getUserId())));
        verify(email).sendEmail(any(), argThat(r -> LEAD_ID.equals(r.getUserId())));
        verifyNoMoreInteractions(sns, email);
    }

    @Test
    void oneFailedChannelIsEnoughIfAnotherDelivers() {
        givenContact(REQUESTER_ID, "+15550000001", "requester@example.org");
        givenContact(LEAD_ID, "+15550000002", null);
        givenNoPendingRecords();
        doThrow(new RuntimeException("SNS down"))
                .when(sns).sendSms(any(), argThat(r -> REQUESTER_ID.equals(r.getUserId())));

        assertDoesNotThrow(() -> service.issueAndSend(request()));

        verify(email).sendEmail(any(), argThat(r -> REQUESTER_ID.equals(r.getUserId())));
    }

    @Test
    void allChannelsFailingForTheRequesterStopsBeforeTheVolunteerIsSent() {
        givenContact(REQUESTER_ID, "+15550000001", "requester@example.org");
        givenContact(LEAD_ID, "+15550000002", "lead@example.org");
        givenNoPendingRecords();
        RuntimeException snsDown = new RuntimeException("SNS down");
        doThrow(snsDown).when(sns).sendSms(any(), any());
        doThrow(new RuntimeException("SES down")).when(email).sendEmail(any(), any());

        OtpDeliveryFailedException e =
                assertThrows(OtpDeliveryFailedException.class, () -> service.issueAndSend(request()));

        assertEquals(OtpParty.REQUESTER, e.getParty());
        assertNotNull(e.getCause());
        verify(sns, never()).sendSms(any(), argThat(r -> LEAD_ID.equals(r.getUserId())));
        verify(email, never()).sendEmail(any(), argThat(r -> LEAD_ID.equals(r.getUserId())));
    }

    @Test
    void missingFieldsAreRejectedBeforeAnyWork() {
        OtpIssueRequestDTO noToken = request();
        noToken.setTaskToken(" ");
        OtpIssueRequestDTO noLead = request();
        noLead.setLeadVolunteerId(null);

        assertThrows(IllegalArgumentException.class, () -> service.issueAndSend(noToken));
        assertThrows(IllegalArgumentException.class, () -> service.issueAndSend(noLead));
        assertThrows(IllegalArgumentException.class, () -> service.issueAndSend(null));
        verifyNoInteractions(contactRepository, otpRepository, sns, email, callback);
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalStateException.class, () -> service(0, MAX_ATTEMPTS).issueAndSend(request()));
        assertThrows(IllegalStateException.class, () -> service(VALIDITY_MINUTES, 0).issueAndSend(request()));
    }

    @Test
    void messageWordingDependsOnTheRecipient() {
        ZonedDateTime expiry = ZonedDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(VALIDITY_MINUTES);

        String toRequester = OtpIssueServiceImpl.messageBody(OtpParty.REQUESTER, "123456", expiry);
        String toVolunteer = OtpIssueServiceImpl.messageBody(OtpParty.LEAD_VOLUNTEER, "654321", expiry);

        assertTrue(toRequester.contains("123456"));
        assertTrue(toRequester.contains("your volunteer"));
        assertTrue(toVolunteer.contains("654321"));
        assertTrue(toVolunteer.contains("the person you are helping"));
        assertTrue(toRequester.contains("18 Nov 2026, 13:00 UTC"));
    }

    // --- helpers -------------------------------------------------------------

    private static OtpIssueRequestDTO request() {
        return OtpIssueRequestDTO.builder()
                .requestId(REQUEST_ID)
                .requesterId(REQUESTER_ID)
                .leadVolunteerId(LEAD_ID)
                .taskToken(TOKEN)
                .build();
    }

    private void givenContact(String userId, String phone, String emailAddress) {
        UserContactRow row = new UserContactRow() {
            public String getUserId() { return userId; }
            public String getEmailAddress() { return emailAddress; }
            public String getPhoneNumber() { return phone; }
        };
        when(contactRepository.findContactByUserId(userId)).thenReturn(Optional.of(row));
    }

    private void givenNoPendingRecords() {
        when(otpRepository.findByRequestIdAndState(REQUEST_ID, "PENDING")).thenReturn(List.of());
    }

    private static OtpVerification pendingRecord(String taskToken) {
        return OtpVerification.builder()
                .otpVerificationId("OTP-OLD")
                .requestId(REQUEST_ID)
                .state(OtpVerificationState.PENDING.name())
                .taskToken(taskToken)
                .build();
    }

    private OtpVerification savedRecord() {
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());
        return captor.getValue();
    }

    /** Asserts one send to the user on the channel, containing their code and not the other party's. */
    private static void assertSent(Object channelService, String userId, NotificationChannel channel,
                                   String address, String expectedCode, String otherCode) {
        ArgumentCaptor<NotificationEvent> event = ArgumentCaptor.forClass(NotificationEvent.class);
        ArgumentCaptor<NotificationRecipient> recipient = ArgumentCaptor.forClass(NotificationRecipient.class);
        if (channelService instanceof SnsNotificationService s) {
            verify(s, atLeastOnce()).sendSms(event.capture(), recipient.capture());
        } else {
            verify((EmailNotificationService) channelService, atLeastOnce()).sendEmail(event.capture(), recipient.capture());
        }

        int index = -1;
        for (int i = 0; i < recipient.getAllValues().size(); i++) {
            if (userId.equals(recipient.getAllValues().get(i).getUserId())) {
                index = i;
            }
        }
        assertTrue(index >= 0, "no " + channel + " sent to " + userId);
        NotificationRecipient r = recipient.getAllValues().get(index);
        NotificationEvent e = event.getAllValues().get(index);
        assertEquals(channel, r.getChannel());
        assertEquals(address, r.getTargetAddress());
        assertEquals(NotificationEventType.OTP_ISSUED, e.getEventType());
        assertEquals(REQUEST_ID, e.getRequestId());
        assertTrue(e.getBody().contains(expectedCode));
        assertFalse(e.getBody().contains(otherCode));
    }

    private static Random sequence(Integer... values) {
        Deque<Integer> queue = new ArrayDeque<>(List.of(values));
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return queue.removeFirst();
            }
        };
    }
}
