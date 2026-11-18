package org.sfa.request.model.enums;

/**
 * The owner of a verification code: the party the code is <em>sent to</em>.
 * The other party enters it after hearing it read out.
 */
public enum OtpParty {
    /** Code sent to the requester; entered by the lead volunteer. */
    REQUESTER,
    /** Code sent to the lead volunteer; entered by the requester. */
    LEAD_VOLUNTEER
}
