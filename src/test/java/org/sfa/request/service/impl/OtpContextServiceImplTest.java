package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sfa.request.dto.OtpContextDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.VolunteerAssignment;
import org.sfa.request.model.enums.OtpContextIneligibleReason;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OtpContextServiceImplTest {

    private static final String REQUEST_ID = "REQ-1";
    private static final String REQUESTER_ID = "SID-REQUESTER";
    private static final String LEAD_ID = "SID-LEAD";

    private final RequestRepository requestRepository = mock(RequestRepository.class);
    private final VolunteerAssignmentRepository volunteerAssignmentRepository = mock(VolunteerAssignmentRepository.class);
    private final OtpContextServiceImpl service =
            new OtpContextServiceImpl(requestRepository, volunteerAssignmentRepository);

    @Test
    void inProgressRequestWithOneLeadIsEligible() {
        givenRequestWithStatus("IN_PROGRESS");
        givenAssignments(lead(LEAD_ID), helping("SID-HELPER"));

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertTrue(context.isEligible());
        assertNull(context.getReason());
        assertEquals(REQUEST_ID, context.getRequestId());
        assertEquals(REQUESTER_ID, context.getRequesterId());
        assertEquals(LEAD_ID, context.getLeadVolunteerId());
        assertEquals("IN_PROGRESS", context.getStatus());
    }

    @Test
    void missingRequestIsIneligible() {
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.REQUEST_NOT_FOUND, context.getReason());
        assertNull(context.getRequesterId());
        assertNull(context.getLeadVolunteerId());
        verifyNoInteractions(volunteerAssignmentRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATED", "MATCHING_VOLUNTEER", "RESOLVED", "CANCELLED", "DELETED", "VOLUNTEER_REASSIGNMENT"})
    void requestNotInProgressIsIneligible(String status) {
        givenRequestWithStatus(status);

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.STATUS_NOT_IN_PROGRESS, context.getReason());
        assertEquals(status, context.getStatus());
        assertNull(context.getLeadVolunteerId());
        verifyNoInteractions(volunteerAssignmentRepository);
    }

    @Test
    void requestWithNoStatusIsIneligible() {
        Request request = request(null);
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.STATUS_NOT_IN_PROGRESS, context.getReason());
        assertNull(context.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"In Progress", "in_progress", " IN_PROGRESS ", "in-progress"})
    void alternativeSpellingsOfInProgressAreEligible(String status) {
        givenRequestWithStatus(status);
        givenAssignments(lead(LEAD_ID));

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertTrue(context.isEligible());
        assertEquals(status, context.getStatus());
    }

    @Test
    void requestWithNoLeadIsIneligible() {
        givenRequestWithStatus("IN_PROGRESS");
        givenAssignments(helping("SID-HELPER"));

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.NO_LEAD_VOLUNTEER, context.getReason());
        assertEquals(REQUESTER_ID, context.getRequesterId());
        assertNull(context.getLeadVolunteerId());
    }

    @Test
    void requestWithNoAssignmentsIsIneligible() {
        givenRequestWithStatus("IN_PROGRESS");
        givenAssignments();

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.NO_LEAD_VOLUNTEER, context.getReason());
    }

    @Test
    void requestWithTwoLeadsIsIneligible() {
        givenRequestWithStatus("IN_PROGRESS");
        givenAssignments(lead(LEAD_ID), lead("SID-OTHER-LEAD"));

        OtpContextDTO context = service.loadContext(REQUEST_ID);

        assertFalse(context.isEligible());
        assertEquals(OtpContextIneligibleReason.MULTIPLE_LEAD_VOLUNTEERS, context.getReason());
        assertNull(context.getLeadVolunteerId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankRequestIdIsRejected(String requestId) {
        assertThrows(IllegalArgumentException.class, () -> service.loadContext(requestId));
        verifyNoInteractions(requestRepository, volunteerAssignmentRepository);
    }

    @Test
    void databaseFailurePropagates() {
        when(requestRepository.findById(anyString())).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class, () -> service.loadContext(REQUEST_ID));
    }

    private void givenRequestWithStatus(String statusName) {
        RequestStatus status = new RequestStatus();
        status.setStatus(statusName);
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request(status)));
    }

    private void givenAssignments(VolunteerAssignment... assignments) {
        when(volunteerAssignmentRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(assignments));
    }

    private static Request request(RequestStatus status) {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER_ID);
        request.setRequestStatus(status);
        return request;
    }

    private static VolunteerAssignment lead(String volunteerId) {
        return assignment(volunteerId, VolunteerAssignmentTypeEnum.LEAD);
    }

    private static VolunteerAssignment helping(String volunteerId) {
        return assignment(volunteerId, VolunteerAssignmentTypeEnum.HELPING);
    }

    private static VolunteerAssignment assignment(String volunteerId, VolunteerAssignmentTypeEnum type) {
        return VolunteerAssignment.builder()
                .requestId(REQUEST_ID)
                .volunteerId(volunteerId)
                .volunteerType(type.getValue())
                .build();
    }
}
