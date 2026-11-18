package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Result of issuing and sending the two verification codes (issue #95).
 *
 * <p>The workflow does not read this: the paused state resumes only when the
 * verification endpoint calls back. It is returned for logging and tests, and
 * deliberately contains no codes or hashes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpIssueResultDTO {

    private String otpVerificationId;

    private String requestId;

    private ZonedDateTime expiresAt;

    /** Earlier PENDING records for this request that were replaced. */
    private int supersededCount;
}
