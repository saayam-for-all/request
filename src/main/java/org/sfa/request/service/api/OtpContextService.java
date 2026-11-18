package org.sfa.request.service.api;

import org.sfa.request.dto.OtpContextDTO;

/**
 * Step S0 of the Part 3 (OTP verification) workflow, issue #95: load a request and
 * its lead volunteer, and check that the request is ready for OTP generation.
 */
public interface OtpContextService {

    /**
     * Loads the requester and lead volunteer for a request and checks that the request
     * is IN_PROGRESS with exactly one LEAD volunteer.
     *
     * <p>Never throws for an ineligible request: it returns {@code eligible = false} with
     * a reason, so the workflow can end cleanly. Database failures propagate, so the
     * workflow's retry policy can handle them.</p>
     *
     * @param requestId the request ID (request.req_id)
     * @return the context for OTP generation
     * @throws IllegalArgumentException if {@code requestId} is null or blank
     */
    OtpContextDTO loadContext(String requestId);
}
