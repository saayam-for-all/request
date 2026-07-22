package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.VolunteerAssignmentDTO;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.VolunteerAssignment;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NotificationEventService;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VolunteerAssignmentServiceImplTest {

    private final VolunteerAssignmentRepository volunteerAssignmentRepository = mock(VolunteerAssignmentRepository.class);
    private final RequestRepository requestRepository = mock(RequestRepository.class);
    private final NotificationEventService notificationEventService = mock(NotificationEventService.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final VolunteerAssignmentServiceImpl service = new VolunteerAssignmentServiceImpl(
            volunteerAssignmentRepository, requestRepository, notificationEventService, messageSource
    );

    private static final String REQUEST_ID = "REQ-1";
    private static final String REQUESTER_ID = "SID-1";

    @Test
    void assignVolunteerAsLeadSavesAssignmentAndNotifiesRequester() {
        Request request = request();
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        when(volunteerAssignmentRepository.findByRequestIdAndVolunteerType(REQUEST_ID, "LEAD"))
                .thenReturn(Optional.empty());
        when(volunteerAssignmentRepository.findByRequestId(REQUEST_ID))
                .thenReturn(List.of(leadAssignment("VOL-1")));
        stubMessages();

        VolunteerAssignmentDTO dto = new VolunteerAssignmentDTO("VOL-1", VolunteerAssignmentTypeEnum.LEAD);
        SaayamResponse<Request> response = service.assignVolunteer(REQUESTER_ID, REQUEST_ID, dto, Locale.US);

        assertTrue(response.isSuccess());
        assertEquals("VOL-1", response.getData().getLeadVolunteerUserId());
        verify(volunteerAssignmentRepository).save(any(VolunteerAssignment.class));
        verify(notificationEventService).enqueueRequestEvent(NotificationEventType.VOLUNTEER_CHOSEN, request, Locale.US);
    }

    @Test
    void reassigningLeadVolunteerUpdatesExistingRowAndNotifiesAgain() {
        Request request = request();
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        VolunteerAssignment existingLead = leadAssignment("VOL-OLD");
        existingLead.setVolunteersAssignedId(99L);
        when(volunteerAssignmentRepository.findByRequestIdAndVolunteerType(REQUEST_ID, "LEAD"))
                .thenReturn(Optional.of(existingLead));
        when(volunteerAssignmentRepository.findByRequestId(REQUEST_ID))
                .thenReturn(List.of(leadAssignment("VOL-NEW")));
        stubMessages();

        VolunteerAssignmentDTO dto = new VolunteerAssignmentDTO("VOL-NEW", VolunteerAssignmentTypeEnum.LEAD);
        service.assignVolunteer(REQUESTER_ID, REQUEST_ID, dto, Locale.US);

        assertEquals("VOL-NEW", existingLead.getVolunteerId());
        verify(volunteerAssignmentRepository).save(existingLead);
        verify(notificationEventService).enqueueRequestEvent(NotificationEventType.VOLUNTEER_CHOSEN, request, Locale.US);
    }

    @Test
    void assignVolunteerAsHelpingDoesNotNotifyRequester() {
        Request request = request();
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        when(volunteerAssignmentRepository.existsByRequestIdAndVolunteerIdAndVolunteerType(REQUEST_ID, "VOL-2", "HELPING"))
                .thenReturn(false);
        when(volunteerAssignmentRepository.findByRequestId(REQUEST_ID))
                .thenReturn(List.of(helpingAssignment("VOL-2")));
        stubMessages();

        VolunteerAssignmentDTO dto = new VolunteerAssignmentDTO("VOL-2", VolunteerAssignmentTypeEnum.HELPING);
        SaayamResponse<Request> response = service.assignVolunteer(REQUESTER_ID, REQUEST_ID, dto, Locale.US);

        assertEquals(List.of("VOL-2"), response.getData().getHelpingVolunteerUserIds());
        verify(notificationEventService, never()).enqueueRequestEvent(any(), any(), any());
    }

    @Test
    void assigningSameHelpingVolunteerTwiceThrowsConflict() {
        Request request = request();
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        when(volunteerAssignmentRepository.existsByRequestIdAndVolunteerIdAndVolunteerType(REQUEST_ID, "VOL-2", "HELPING"))
                .thenReturn(true);
        stubMessages();

        VolunteerAssignmentDTO dto = new VolunteerAssignmentDTO("VOL-2", VolunteerAssignmentTypeEnum.HELPING);
        assertThrows(ConflictException.class, () -> service.assignVolunteer(REQUESTER_ID, REQUEST_ID, dto, Locale.US));
        verify(volunteerAssignmentRepository, never()).save(any());
    }

    @Test
    void removeVolunteerDeletesExistingAssignment() {
        Request request = request();
        VolunteerAssignment assignment = helpingAssignment("VOL-2");
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        when(volunteerAssignmentRepository.findByRequestIdAndVolunteerId(REQUEST_ID, "VOL-2"))
                .thenReturn(Optional.of(assignment));
        stubMessages();

        SaayamResponse<Void> response = service.removeVolunteer(REQUESTER_ID, REQUEST_ID, "VOL-2", Locale.US);

        assertTrue(response.isSuccess());
        verify(volunteerAssignmentRepository).delete(assignment);
    }

    @Test
    void removeVolunteerThrowsNotFoundWhenNoAssignmentExists() {
        Request request = request();
        when(requestRepository.findActiveByRequestIdAndRequesterId(eq(REQUEST_ID), eq(REQUESTER_ID), anyInt()))
                .thenReturn(Optional.of(request));
        when(volunteerAssignmentRepository.findByRequestIdAndVolunteerId(REQUEST_ID, "VOL-2"))
                .thenReturn(Optional.empty());
        stubMessages();

        assertThrows(NotFoundException.class,
                () -> service.removeVolunteer(REQUESTER_ID, REQUEST_ID, "VOL-2", Locale.US));
    }

    @Test
    void populateAssignmentsSplitsLeadAndHelpingCorrectly() {
        Request request = request();
        when(volunteerAssignmentRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(
                leadAssignment("VOL-LEAD"),
                helpingAssignment("VOL-HELP-1"),
                helpingAssignment("VOL-HELP-2")
        ));

        service.populateAssignments(request);

        assertEquals("VOL-LEAD", request.getLeadVolunteerUserId());
        assertEquals(List.of("VOL-HELP-1", "VOL-HELP-2"), request.getHelpingVolunteerUserIds());
    }

    private void stubMessages() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("message");
    }

    private Request request() {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER_ID);
        return request;
    }

    private VolunteerAssignment leadAssignment(String volunteerId) {
        return VolunteerAssignment.builder()
                .requestId(REQUEST_ID)
                .volunteerId(volunteerId)
                .volunteerType("LEAD")
                .build();
    }

    private VolunteerAssignment helpingAssignment(String volunteerId) {
        return VolunteerAssignment.builder()
                .requestId(REQUEST_ID)
                .volunteerId(volunteerId)
                .volunteerType("HELPING")
                .build();
    }
}
