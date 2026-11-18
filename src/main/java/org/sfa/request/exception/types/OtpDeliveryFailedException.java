package org.sfa.request.exception.types;

import lombok.Getter;
import org.sfa.request.model.enums.OtpParty;

/**
 * Every delivery attempt (SMS and email) failed for one party's code (issue #95).
 *
 * <p><b>Retryable:</b> usually a transient SNS/SES failure. A retry issues fresh codes
 * and supersedes the record created by the failed attempt. In the state machine,
 * match it by its fully qualified class name
 * ({@code org.sfa.request.exception.types.OtpDeliveryFailedException}).</p>
 */
@Getter
public class OtpDeliveryFailedException extends BaseException {

    private final OtpParty party;
    private final String userId;

    public OtpDeliveryFailedException(OtpParty party, String userId, Throwable cause) {
        super("Could not deliver verification code to " + party + " " + userId, cause);
        this.party = party;
        this.userId = userId;
    }
}
