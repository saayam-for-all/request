package org.sfa.request.model.enums;

/**
 * Lifecycle of one OTP verification record (Part 3 of the workflow, issue #95).
 * Stored by name in otp_verification.state.
 */
public enum OtpVerificationState {
    /** Codes issued; waiting for both parties to verify. At most one per request. */
    PENDING,
    /** Both codes were entered correctly. */
    VERIFIED,
    /** A code ran out of attempts. */
    EXHAUSTED,
    /** The codes passed their expiry time. */
    EXPIRED,
    /** Replaced by a newer record, e.g. because the lead volunteer changed. */
    SUPERSEDED
}
