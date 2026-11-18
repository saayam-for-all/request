package org.sfa.request.model.enums;

/**
 * Why a request cannot enter Part 3 (OTP verification) of the help-request
 * workflow (issue #95, step S0). Returned to Step Functions so the workflow can
 * end cleanly instead of failing.
 */
public enum OtpContextIneligibleReason {
    /** No request row exists for the given request ID. */
    REQUEST_NOT_FOUND,
    /** The request exists but its status is not IN_PROGRESS. */
    STATUS_NOT_IN_PROGRESS,
    /** The request has no LEAD row in volunteers_assigned. */
    NO_LEAD_VOLUNTEER,
    /** The request has more than one LEAD row in volunteers_assigned. */
    MULTIPLE_LEAD_VOLUNTEERS
}
