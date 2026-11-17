package org.sfa.request.stepfunctions;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import static org.sfa.request.stepfunctions.Part4RatingStore.Purpose;
import static org.sfa.request.stepfunctions.Part4RatingStore.Submission;
import static org.sfa.request.stepfunctions.WorkflowFinalizationTask.validId;

/** Internal adapter for #79. The authenticated user comes from the caller's security context. */
@Component
public class WorkflowRatingCallbacks {
    private final ObjectProvider<Part4RatingStore> stores;

    public WorkflowRatingCallbacks(ObjectProvider<Part4RatingStore> stores) {
        this.stores = stores;
    }

    public Part4RatingStore.Registration register(String requestId, String executionId, Purpose purpose, String taskToken) {
        if (!validId(requestId) || !validId(executionId, 512) || purpose == null || taskToken == null || taskToken.isBlank()) {
            return Part4RatingStore.Registration.REJECTED;
        }
        Part4RatingStore store = stores.getIfAvailable();
        if (store == null) {
            throw new IllegalStateException("Rating registration store unavailable");
        }
        return store.registerAndQueueInvitation(requestId, executionId, purpose, taskToken);
    }

    public Submission submit(String requestId, String executionId, Purpose purpose, String authenticatedUserId,
                             int score, String feedback) {
        if (!validId(authenticatedUserId)) {
            return Submission.FORBIDDEN;
        }
        if (!validId(requestId) || !validId(executionId, 512) || purpose == null || score < 0 || score > 5) {
            return Submission.INVALID;
        }
        Part4RatingStore store = stores.getIfAvailable();
        return store == null ? Submission.UNAVAILABLE
                : store.recordAndQueueAcknowledgement(requestId, executionId, purpose, authenticatedUserId, score, feedback);
    }
}
