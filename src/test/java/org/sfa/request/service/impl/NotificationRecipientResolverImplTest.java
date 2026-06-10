package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestGuestDetails;
import org.sfa.request.repository.RequestGuestDetailsRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationRecipientResolverImplTest {

    private final RequestGuestDetailsRepository requestGuestDetailsRepository = mock(RequestGuestDetailsRepository.class);
    private final NotificationRecipientResolverImpl resolver =
            new NotificationRecipientResolverImpl(requestGuestDetailsRepository);

    @Test
    void resolveRequestRecipientsUsesGuestEmailAndPhoneWhenPresent() {
        Request request = request();
        RequestGuestDetails guestDetails = RequestGuestDetails.builder()
                .requestId("REQ-1")
                .reqEmail("guest@example.org")
                .reqPhone("+15555550100")
                .build();
        when(requestGuestDetailsRepository.findById("REQ-1")).thenReturn(Optional.of(guestDetails));

        List<NotificationRecipient> recipients = resolver.resolveRequestRecipients(request);

        assertEquals(2, recipients.size());
        assertEquals(NotificationChannel.EMAIL, recipients.get(0).getChannel());
        assertEquals("guest@example.org", recipients.get(0).getTargetAddress());
        assertEquals(NotificationChannel.SMS, recipients.get(1).getChannel());
        assertEquals("+15555550100", recipients.get(1).getTargetAddress());
    }

    @Test
    void resolveRequestRecipientsFallsBackToRequesterWhenNoContactDetailsExist() {
        Request request = request();
        when(requestGuestDetailsRepository.findById("REQ-1")).thenReturn(Optional.empty());

        List<NotificationRecipient> recipients = resolver.resolveRequestRecipients(request);

        assertEquals(1, recipients.size());
        assertEquals("USER-1", recipients.get(0).getUserId());
        assertEquals(NotificationChannel.EMAIL, recipients.get(0).getChannel());
        assertNull(recipients.get(0).getTargetAddress());
    }

    private Request request() {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId("USER-1");
        return request;
    }
}
