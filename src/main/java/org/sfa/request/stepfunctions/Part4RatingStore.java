package org.sfa.request.stepfunctions;

/** The transactional boundary to be implemented against the approved callback and rating schemas. */
public interface Part4RatingStore {
    enum Purpose { Requester, Volunteer }
    enum Registration { REGISTERED, ALREADY_REGISTERED, TERMINATED, REJECTED }
    enum Submission { RECORDED, DUPLICATE, FORBIDDEN, EXPIRED, TERMINATED, INVALID, UNAVAILABLE }

    /**
     * Require a committed RESOLVED request for this execution. Resolve the participant from the
     * verified assignment and beneficiary policy. Register the encrypted token and enqueue exactly
     * one invitation intent for this execution and purpose in the same transaction. Publish only
     * after commit. Registration retries must reuse the invitation and validate current wait ownership.
     */
    Registration registerAndQueueInvitation(String requestId, String executionId, Purpose purpose, String taskToken);

    /**
     * Atomically validate correlation, resolved/uncancelled state, authenticated participant,
     * token expiry and the approved feedback policy. Lock against concurrent submission or cancellation.
     * Persist at most one rating per request and purpose and queue its acknowledgement in the same transaction.
     * A relay sends only the outcome RECORDED after commit. A failed write must leave the wait
     * pending and retryable. A second submission must never overwrite the first rating.
     */
    Submission recordAndQueueAcknowledgement(String requestId, String executionId, Purpose purpose,
                                             String authenticatedUserId, int score, String feedback);
}
