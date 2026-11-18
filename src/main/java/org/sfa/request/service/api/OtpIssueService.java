package org.sfa.request.service.api;

import org.sfa.request.dto.OtpIssueRequestDTO;
import org.sfa.request.dto.OtpIssueResultDTO;

/**
 * Steps S1–S3 of Part 3 (OTP verification, issue #95): generate the two codes, store
 * their hashes with the workflow's task token, and send each party their own code.
 */
public interface OtpIssueService {

    /**
     * Issues and sends a fresh pair of codes for a request.
     *
     * <p>Any earlier PENDING record for the request is marked SUPERSEDED and its paused
     * execution, if still waiting, is resumed with {@code {"result": "SUPERSEDED"}}.</p>
     *
     * @throws IllegalArgumentException if a required field is missing
     * @throws org.sfa.request.exception.types.OtpRecipientUnreachableException
     *         if either party has no phone number or email on file (not retryable)
     * @throws org.sfa.request.exception.types.OtpDeliveryFailedException
     *         if every send to one party failed (retryable)
     */
    OtpIssueResultDTO issueAndSend(OtpIssueRequestDTO request);
}
