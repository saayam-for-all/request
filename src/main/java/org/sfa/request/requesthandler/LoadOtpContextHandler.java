package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.config.SpringContext;
import org.sfa.request.dto.OtpContextDTO;
import org.sfa.request.service.api.OtpContextService;

import java.util.Map;

/**
 * Lambda entry point for step S0 of the Part 3 (OTP verification) workflow, issue #95.
 *
 * <p>Invoked directly by Step Functions (not through API Gateway) with
 * {@code {"requestId": "..."}}. Other fields in the input are ignored.</p>
 *
 * <p>Returns an {@link OtpContextDTO}. An ineligible request is a normal result
 * ({@code eligible = false}), for a Choice state to route on. A missing requestId
 * throws {@link IllegalArgumentException}; database failures propagate so the
 * Task state's Retry policy can handle them.</p>
 */
@Slf4j
public class LoadOtpContextHandler implements RequestHandler<Map<String, Object>, OtpContextDTO> {

    static final String REQUEST_ID_FIELD = "requestId";

    private OtpContextService otpContextService;

    /** Used by the Lambda runtime; the Spring context is created on first invocation. */
    public LoadOtpContextHandler() {
    }

    /** Used by tests to supply the service directly. */
    LoadOtpContextHandler(OtpContextService otpContextService) {
        this.otpContextService = otpContextService;
    }

    @Override
    public OtpContextDTO handleRequest(Map<String, Object> input, Context lambdaContext) {
        Object rawRequestId = input == null ? null : input.get(REQUEST_ID_FIELD);
        if (rawRequestId == null) {
            throw new IllegalArgumentException("Input must contain " + REQUEST_ID_FIELD);
        }
        return getOtpContextService().loadContext(rawRequestId.toString());
    }

    private OtpContextService getOtpContextService() {
        if (otpContextService == null) {
            otpContextService = SpringContext.getContext().getBean(OtpContextService.class);
        }
        return otpContextService;
    }
}
