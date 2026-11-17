package org.sfa.request.stepfunctions;

import java.util.List;
import java.util.Optional;

/** Integration boundary for the authenticated callback service (#79) and verified assignment (#95). */
public interface Part4WorkStore {
    enum Action { ADD_HELPERS, DELEGATE, COMPLETE, CANCEL }

    record Decision(Action action, String authenticatedLeadId, List<String> helperIds, String organizationId,
                    boolean applied) { }

    /**
     * Called within the Request database transaction after locking the request row.
     * Return only a current, unexpired decision for this request and execution, based on a durable
     * approved assignment, completed two-party OTP verification, and an authenticated lead.
     * The decision must have been committed before its work token was acknowledged. Decisions from
     * previous waits and assignments must be excluded. The current applied decision may be
     * returned for a Lambda retry. For ADD_HELPERS, all returned helpers must be currently eligible
     * and authorized for assignment under the approved volunteer policy.
     */
    Optional<Decision> lockVerifiedDecision(String requestId, String executionId, String decisionId);

    /** Commit the application marker atomically with the request or assignment changes. */
    void markApplied(String requestId, String executionId, String decisionId);
}
