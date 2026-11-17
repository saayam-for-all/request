package org.sfa.request.stepfunctions;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.service.api.EmailNotificationService;
import org.sfa.request.service.api.InAppNotificationService;
import org.sfa.request.service.api.SnsNotificationService;
import org.sfa.request.service.api.SQSService;
import org.sfa.request.service.impl.NotificationDispatchServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sfa.request.stepfunctions.Part4RatingStore.Purpose.*;
import static org.sfa.request.stepfunctions.Part4RatingStore.Registration;
import static org.sfa.request.stepfunctions.Part4RatingStore.Submission;

class WorkflowRatingCallbacksTest {
    private final ObjectProvider<Part4RatingStore> provider = mock();
    private final WorkflowRatingCallbacks callbacks = new WorkflowRatingCallbacks(provider);

    @Test
    void invalidAndUnavailableSubmissionsCannotBeAcknowledged() {
        assertEquals(Submission.FORBIDDEN, callbacks.submit("req", "exec", Requester, "", 4, null));
        assertEquals(Submission.INVALID, callbacks.submit("req", "exec", Requester, "beneficiary", 6, null));
        assertEquals(Submission.UNAVAILABLE, callbacks.submit("req", "exec", Requester, "beneficiary", 4, null));
        assertThrows(IllegalStateException.class, () -> callbacks.register("req", "exec", Requester, "opaque-token"));
    }

    @Test
    void independentSubmissionsCanRetryAfterStorageFailureAndNeverOverwrite() {
        for (var order : List.of(List.of(Requester, Volunteer), List.of(Volunteer, Requester))) {
            var store = new LocalRatingStore();
            when(provider.getIfAvailable()).thenReturn(store);
            var first = order.get(0);
            var second = order.get(1);
            assertEquals(Registration.REGISTERED, callbacks.register("req", "exec", first, "opaque-token"));
            assertEquals(Registration.ALREADY_REGISTERED, callbacks.register("req", "exec", first, "opaque-token"));
            assertEquals(Registration.REGISTERED, callbacks.register("req", "exec", second, "another-token"));
            assertEquals(Submission.FORBIDDEN, callbacks.submit("req", "exec", first, second.name(), 4, null));
            assertEquals(Submission.EXPIRED, callbacks.submit("req", "old-exec", first, first.name(), 4, null));
            store.expired.add(first);
            assertEquals(Submission.EXPIRED, callbacks.submit("req", "exec", first, first.name(), 4, null));
            store.expired.remove(first);
            store.failWrites = true;
            assertThrows(IllegalStateException.class,
                    () -> callbacks.submit("req", "exec", first, first.name(), 4, null));
            assertTrue(store.acknowledgements.isEmpty());
            assertTrue(store.ratings.isEmpty());
            store.failWrites = false;
            assertEquals(Submission.RECORDED, callbacks.submit("req", "exec", first, first.name(), 4, null));
            assertEquals(Submission.DUPLICATE, callbacks.submit("req", "exec", first, first.name(), 1, null));
            assertEquals(Submission.RECORDED, callbacks.submit("req", "exec", second, second.name(), 5, null));
            assertEquals(4, store.ratings.get(first));
            assertEquals(5, store.ratings.get(second));
            assertEquals(EnumSet.allOf(Part4RatingStore.Purpose.class), store.acknowledgements);
        }
    }

    @Test
    void invitationUsesExistingQueueAndRefusesWrongParticipantOrOpenTransaction() {
        SQSService queue = mock();
        var publisher = new RatingInvitationPublisher(queue);
        var recipient = new NotificationRecipient("beneficiary", NotificationChannel.EMAIL, "example@example.test");
        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish("invite-1", "req", Requester, "wrong", recipient));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            assertThrows(IllegalStateException.class,
                    () -> publisher.publish("invite-1", "req", Requester, "beneficiary", recipient));
        } finally {
            TransactionSynchronizationManager.clear();
        }
        verifyNoInteractions(queue);
        publisher.publish("invite-1", "req", Requester, "beneficiary", recipient);

        var event = org.mockito.ArgumentCaptor.forClass(NotificationEvent.class);
        verify(queue).sendMessage(event.capture());
        assertEquals(NotificationEventType.RATING_REQUESTED, event.getValue().getEventType());
        assertEquals(List.of(recipient), event.getValue().getRecipients());
        assertEquals("invite-1", event.getValue().getAttributes().get("invitationId"));
        assertEquals("Requester", event.getValue().getAttributes().get("purpose"));
        assertFalse(event.getValue().toString().contains(recipient.getTargetAddress()));
        assertFalse(recipient.toString().contains(recipient.getTargetAddress()));
        var email = mock(EmailNotificationService.class);
        var sns = mock(SnsNotificationService.class);
        var inApp = mock(InAppNotificationService.class);
        new NotificationDispatchServiceImpl(email, sns, inApp).dispatch(event.getValue());
        verify(email).sendEmail(event.getValue(), recipient);
        verifyNoInteractions(sns, inApp);
    }

    /** Deterministic stand-in for the unavailable Database and #79 implementation. */
    private static final class LocalRatingStore implements Part4RatingStore {
        private final EnumSet<Purpose> registrations = EnumSet.noneOf(Purpose.class);
        private final EnumSet<Purpose> expired = EnumSet.noneOf(Purpose.class);
        private final EnumSet<Purpose> acknowledgements = EnumSet.noneOf(Purpose.class);
        private final EnumMap<Purpose, Integer> ratings = new EnumMap<>(Purpose.class);
        private boolean failWrites;

        @Override
        public Registration registerAndQueueInvitation(String request, String execution, Purpose purpose, String token) {
            return registrations.add(purpose) ? Registration.REGISTERED : Registration.ALREADY_REGISTERED;
        }

        @Override
        public Submission recordAndQueueAcknowledgement(String request, String execution, Purpose purpose, String user,
                                                         int score, String feedback) {
            if (!"req".equals(request) || !"exec".equals(execution)
                    || !registrations.contains(purpose) || expired.contains(purpose)) {
                return Submission.EXPIRED;
            }
            if (!user.equals(purpose.name())) {
                return Submission.FORBIDDEN;
            }
            if (failWrites) {
                throw new IllegalStateException("Simulated transaction rollback");
            }
            if (ratings.putIfAbsent(purpose, score) != null) {
                return Submission.DUPLICATE;
            }
            acknowledgements.add(purpose);
            return Submission.RECORDED;
        }
    }
}
