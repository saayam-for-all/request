package org.sfa.request.stepfunctions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.VolunteerAssignment;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sfa.request.stepfunctions.Part4WorkStore.Action.*;
import static org.sfa.request.stepfunctions.WorkflowFinalizationTask.Outcome.*;

class WorkflowFinalizationTaskTest {
    private final RequestRepository requests = mock(RequestRepository.class);
    private final RequestStatusRepository statuses = mock(RequestStatusRepository.class);
    private final VolunteerAssignmentRepository assignments = mock(VolunteerAssignmentRepository.class);
    private final VolunteerAssignmentService volunteers = mock(VolunteerAssignmentService.class);
    private final Part4WorkStore store = mock(Part4WorkStore.class);
    private final ObjectProvider<Part4WorkStore> provider = mock();
    private final RequestStatus active = status(61, "IN_PROGRESS");
    private final Request request = Request.builder().requestId("request").requestStatus(active).build();
    private WorkflowFinalizationTask tasks;

    @BeforeEach
    void setUp() {
        tasks = new WorkflowFinalizationTask(requests, statuses, assignments, volunteers, provider, false);
        when(provider.getIfAvailable()).thenReturn(store);
        when(requests.findByIdForUpdate("request")).thenReturn(Optional.of(request));
        when(assignments.findByRequestId("request")).thenReturn(List.of(lead("lead")));
    }

    @Test
    void noWorkStoreMeansNoDatabaseOperation() {
        when(provider.getIfAvailable()).thenReturn(null);
        assertEquals(UNAVAILABLE, tasks.apply(COMPLETE, "request", "execution", "decision"));
        verifyNoInteractions(requests, statuses, assignments, volunteers);
    }

    @Test
    void requiresVerifiedCurrentDecisionAndCurrentLead() {
        decision(COMPLETE, "former-lead", null, null, false);
        assertEquals(REJECTED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        assertEquals(REJECTED, tasks.apply(COMPLETE, "request", "stale-execution", "decision"));
        assertEquals(REJECTED, tasks.apply(ADD_HELPERS, "request", "execution", "decision"));
        decision(COMPLETE, "lead", null, null, false);
        when(assignments.findByRequestId("request")).thenReturn(List.of(lead("lead"), lead("other")));
        assertEquals(REJECTED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        verifyNoInteractions(statuses, volunteers);
        verify(store, never()).markApplied(anyString(), anyString(), anyString());
    }

    @Test
    void helperBatchAndItsRetryReuseAssignmentsWithoutReplacingLead() {
        List<String> ids = List.of("helper-1", "helper-2");
        decision(ADD_HELPERS, "lead", ids, null, false);
        when(volunteers.addMissingWorkflowHelpers("request", ids)).thenReturn(true);
        assertEquals(HELPERS_ASSIGNED, tasks.apply(ADD_HELPERS, "request", "execution", "decision"));
        decision(ADD_HELPERS, "lead", ids, null, true);
        assertEquals(HELPERS_ASSIGNED, tasks.apply(ADD_HELPERS, "request", "execution", "decision"));
        verify(volunteers, times(1)).addMissingWorkflowHelpers("request", ids);
        verify(store, times(1)).markApplied("request", "execution", "decision");
        assertSame(active, request.getRequestStatus());
    }

    @Test
    void helperBatchWithLeadOrMalformedIdIsRejectedBeforeWriting() {
        for (String id : List.of("lead", "", "space ", "bad\nvalue")) {
            decision(ADD_HELPERS, "lead", List.of("helper", id), null, false);
            assertEquals(REJECTED, tasks.apply(ADD_HELPERS, "request", "execution", "decision"));
        }
        verifyNoInteractions(volunteers);
    }

    @Test
    void awaitingStatusApprovalCannotChangeRequest() {
        decision(COMPLETE, "lead", null, null, false);
        assertEquals(UNAVAILABLE, tasks.apply(COMPLETE, "request", "execution", "decision"));
        assertSame(active, request.getRequestStatus());
        verifyNoInteractions(statuses);
    }

    @Test
    void approvedResolutionUsesOnlyUniqueSemanticRowsAndSameDecisionForReplay() {
        tasks = new WorkflowFinalizationTask(requests, statuses, assignments, volunteers, provider, true);
        decision(COMPLETE, "lead", null, null, false);
        RequestStatus resolved = status(87, "RESOLVED");
        when(statuses.findByStatusIgnoreCase("IN_PROGRESS")).thenReturn(List.of(active));
        when(statuses.findByStatusIgnoreCase("RESOLVED")).thenReturn(List.of(resolved, status(98, "resolved")));
        assertEquals(UNAVAILABLE, tasks.apply(COMPLETE, "request", "execution", "decision"));
        assertSame(active, request.getRequestStatus());

        when(statuses.findByStatusIgnoreCase("RESOLVED")).thenReturn(List.of(resolved));
        assertEquals(RESOLVED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        assertSame(resolved, request.getRequestStatus());
        var servicedAt = request.getServicedAt();
        assertNotNull(servicedAt);
        assertEquals(REJECTED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        decision(COMPLETE, "lead", null, null, true);
        assertEquals(RESOLVED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        assertEquals(servicedAt, request.getServicedAt());
        verify(store, times(1)).markApplied("request", "execution", "decision");
    }

    @Test
    void terminalRequestsCannotBeRestoredAndCancelRequiresCommittedStatus() {
        decision(CANCEL, "lead", null, null, false);
        assertEquals(REJECTED, tasks.apply(CANCEL, "request", "execution", "decision"));
        request.setRequestStatus(status(82, "CANCELLED"));
        assertEquals(TERMINATED, tasks.apply(CANCEL, "request", "execution", "decision"));
        request.setRequestStatus(status(83, "DELETED"));
        decision(COMPLETE, "lead", null, null, true);
        assertEquals(TERMINATED, tasks.apply(COMPLETE, "request", "execution", "decision"));
        verifyNoInteractions(statuses, volunteers);
    }

    @Test
    void delegationRequiresOrganizationAndFailsClosedWithoutHandoff() {
        decision(DELEGATE, "lead", null, "", false);
        assertEquals(REJECTED, tasks.apply(DELEGATE, "request", "execution", "decision"));
        decision(DELEGATE, "lead", null, "org-1", false);
        assertEquals(DELEGATION_UNAVAILABLE, tasks.apply(DELEGATE, "request", "execution", "decision"));
        assertSame(active, request.getRequestStatus());
        verify(store, never()).markApplied(anyString(), anyString(), anyString());
    }

    @Test
    void handlerRequiresCorrelationAndSanitizesDependencyErrors() {
        var handler = new WorkflowFinalizationTaskHandler(tasks);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(
                Map.of("operation", "resolveIfActive", "requestId", "request", "executionId", "execution"), null));
        when(requests.findByIdForUpdate("request")).thenThrow(new IllegalStateException("private detail"));
        var exception = assertThrows(IllegalStateException.class, () -> handler.handleRequest(Map.of(
                "operation", "resolveIfActive", "requestId", "request",
                "executionId", "execution", "decisionId", "decision"), null));
        assertEquals("Finalization dependency failed", exception.getMessage());
        assertNull(exception.getCause());
    }

    private void decision(Part4WorkStore.Action action, String lead, List<String> helpers, String org, boolean applied) {
        when(store.lockVerifiedDecision("request", "execution", "decision"))
                .thenReturn(Optional.of(new Part4WorkStore.Decision(action, lead, helpers, org, applied)));
    }

    private static RequestStatus status(int id, String name) {
        var status = new RequestStatus();
        status.setRequestStatusId(id);
        status.setStatus(name);
        return status;
    }

    private static VolunteerAssignment lead(String id) {
        return VolunteerAssignment.builder().volunteerId(id).volunteerType("LEAD").build();
    }
}
