package org.sfa.request.exception.types;

import lombok.Getter;
import org.sfa.request.model.enums.OtpParty;

/**
 * A party to OTP verification has no phone number or email address on file, so
 * their code cannot be delivered (issue #95).
 *
 * <p><b>Not retryable:</b> retrying will not add contact details. In the state machine,
 * catch it by its fully qualified class name
 * ({@code org.sfa.request.exception.types.OtpRecipientUnreachableException}), which is
 * the error name the Java Lambda runtime reports.</p>
 */
@Getter
public class OtpRecipientUnreachableException extends BaseException {

    private final OtpParty party;
    private final String userId;

    public OtpRecipientUnreachableException(OtpParty party, String userId) {
        super("No phone number or email address on file for " + party + " " + userId);
        this.party = party;
        this.userId = userId;
    }
}
