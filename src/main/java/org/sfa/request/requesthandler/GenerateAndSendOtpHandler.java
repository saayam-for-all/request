package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.config.SpringContext;
import org.sfa.request.dto.OtpIssueRequestDTO;
import org.sfa.request.dto.OtpIssueResultDTO;
import org.sfa.request.service.api.OtpIssueService;

import java.util.Map;

/**
 * Lambda entry point for steps S1–S3 of Part 3 (OTP verification), issue #95.
 *
 * <p>Invoked by Step Functions with the {@code .waitForTaskToken} integration, so the
 * execution stays paused after this returns, until the verification endpoint resumes it.
 * Expected input (other fields are ignored):</p>
 *
 * <pre>{@code
 * {
 *   "requestId":       "...",   // from S0
 *   "requesterId":     "...",   // from S0
 *   "leadVolunteerId": "...",   // from S0
 *   "taskToken":       "..."    // $$.Task.Token
 * }
 * }</pre>
 *
 * <p>Errors, by fully qualified class name: {@code IllegalArgumentException} (bad input),
 * {@code org.sfa.request.exception.types.OtpRecipientUnreachableException} (no contact
 * details; do not retry) and {@code org.sfa.request.exception.types.OtpDeliveryFailedException}
 * (SNS/SES failure; retry).</p>
 */
@Slf4j
public class GenerateAndSendOtpHandler implements RequestHandler<Map<String, Object>, OtpIssueResultDTO> {

    private OtpIssueService otpIssueService;

    /** Used by the Lambda runtime; the Spring context is created on first invocation. */
    public GenerateAndSendOtpHandler() {
    }

    /** Used by tests to supply the service directly. */
    GenerateAndSendOtpHandler(OtpIssueService otpIssueService) {
        this.otpIssueService = otpIssueService;
    }

    @Override
    public OtpIssueResultDTO handleRequest(Map<String, Object> input, Context lambdaContext) {
        if (input == null) {
            throw new IllegalArgumentException("Input is required");
        }
        OtpIssueRequestDTO request = OtpIssueRequestDTO.builder()
                .requestId(text(input, "requestId"))
                .requesterId(text(input, "requesterId"))
                .leadVolunteerId(text(input, "leadVolunteerId"))
                .taskToken(text(input, "taskToken"))
                .build();
        return getOtpIssueService().issueAndSend(request);
    }

    private static String text(Map<String, Object> input, String field) {
        Object value = input.get(field);
        return value == null ? null : value.toString();
    }

    private OtpIssueService getOtpIssueService() {
        if (otpIssueService == null) {
            otpIssueService = SpringContext.getContext().getBean(OtpIssueService.class);
        }
        return otpIssueService;
    }
}
