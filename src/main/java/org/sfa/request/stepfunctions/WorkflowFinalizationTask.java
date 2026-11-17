package org.sfa.request.stepfunctions;

import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.sfa.request.stepfunctions.Part4WorkStore.Action;

@Component
public class WorkflowFinalizationTask {
    enum Outcome { HELPERS_ASSIGNED, RESOLVED, TERMINATED, DELEGATION_UNAVAILABLE, REJECTED, UNAVAILABLE }

    private final RequestRepository requests;
    private final RequestStatusRepository statuses;
    private final VolunteerAssignmentRepository assignments;
    private final VolunteerAssignmentService volunteers;
    private final ObjectProvider<Part4WorkStore> workStores;
    private final boolean statusWritesApproved;

    public WorkflowFinalizationTask(RequestRepository requests, RequestStatusRepository statuses,
                                    VolunteerAssignmentRepository assignments, VolunteerAssignmentService volunteers,
                                    ObjectProvider<Part4WorkStore> workStores,
                                    @Value("${saayam.workflow.part4.status-writes-approved:false}") boolean statusWritesApproved) {
        this.requests = requests;
        this.statuses = statuses;
        this.assignments = assignments;
        this.volunteers = volunteers;
        this.workStores = workStores;
        this.statusWritesApproved = statusWritesApproved;
    }

    @Transactional
    public Outcome apply(Action action, String requestId, String executionId, String decisionId) {
        Part4WorkStore store = workStores.getIfAvailable();
        if (store == null) {
            return Outcome.UNAVAILABLE;
        }
        Request request = requests.findByIdForUpdate(requestId).orElse(null);
        if (request == null) {
            return Outcome.REJECTED;
        }
        Part4WorkStore.Decision decision = store.lockVerifiedDecision(requestId, executionId, decisionId).orElse(null);
        if (decision == null || decision.action() != action) {
            return Outcome.REJECTED;
        }
        if (hasStatus(request, "CANCELLED") || hasStatus(request, "DELETED")) {
            return Outcome.TERMINATED;
        }
        if (!isCurrentLead(requestId, decision.authenticatedLeadId())) {
            return Outcome.REJECTED;
        }
        if (action == Action.COMPLETE && decision.applied()) {
            return hasStatus(request, "RESOLVED") ? Outcome.RESOLVED : Outcome.REJECTED;
        }
        if (!hasStatus(request, "IN_PROGRESS")) {
            return Outcome.REJECTED;
        }
        Outcome outcome = switch (action) {
            case ADD_HELPERS -> addHelpers(request, decision);
            case COMPLETE -> resolveIfActive(request);
            case DELEGATE -> validId(decision.organizationId()) ? Outcome.DELEGATION_UNAVAILABLE : Outcome.REJECTED;
            case CANCEL -> Outcome.REJECTED;
        };
        if (!decision.applied() && (outcome == Outcome.RESOLVED || outcome == Outcome.HELPERS_ASSIGNED)) {
            store.markApplied(requestId, executionId, decisionId);
        }
        return outcome;
    }

    private boolean isCurrentLead(String requestId, String userId) {
        if (!validId(userId)) {
            return false;
        }
        var leads = assignments.findByRequestId(requestId).stream()
                .filter(row -> VolunteerAssignmentTypeEnum.LEAD.getValue().equals(row.getVolunteerType()))
                .toList();
        return leads.size() == 1 && userId.equals(leads.get(0).getVolunteerId());
    }

    private Outcome addHelpers(Request request, Part4WorkStore.Decision decision) {
        List<String> ids = decision.helperIds();
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> !validId(id)
                || id.equals(decision.authenticatedLeadId()))) {
            return Outcome.REJECTED;
        }
        return decision.applied() || volunteers.addMissingWorkflowHelpers(request.getRequestId(), ids)
                ? Outcome.HELPERS_ASSIGNED : Outcome.REJECTED;
    }

    private Outcome resolveIfActive(Request request) {
        if (!statusWritesApproved) {
            return Outcome.UNAVAILABLE;
        }
        List<RequestStatus> active = statuses.findByStatusIgnoreCase("IN_PROGRESS");
        List<RequestStatus> resolved = statuses.findByStatusIgnoreCase("RESOLVED");
        if (active.size() != 1 || resolved.size() != 1
                || !active.get(0).getRequestStatusId().equals(request.getRequestStatus().getRequestStatusId())) {
            return Outcome.UNAVAILABLE;
        }
        ZonedDateTime now = ZonedDateTime.now();
        request.setRequestStatus(resolved.get(0));
        request.setServicedAt(now);
        request.setLastUpdatedAt(now);
        return Outcome.RESOLVED;
    }

    static boolean validId(String id) {
        return validId(id, 255);
    }

    static boolean validId(String id, int maxLength) {
        return id != null && !id.isBlank() && id.length() <= maxLength && id.equals(id.strip())
                && id.chars().noneMatch(Character::isISOControl);
    }

    private static boolean hasStatus(Request request, String name) {
        RequestStatus status = request.getRequestStatus();
        return status != null && name.equalsIgnoreCase(status.getStatus());
    }
}
