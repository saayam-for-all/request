package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.ExecutionAlreadyExistsException;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Opt-in StartExecution boundary. It is not called by Create Request until Issue #80 is resolved. */
public final class WorkflowExecutionStarter {
    private final SfnClient client;
    private final ObjectMapper mapper;
    private final String stateMachineArn;

    public WorkflowExecutionStarter(SfnClient client, ObjectMapper mapper, String stateMachineArn) {
        if (!StringUtils.hasText(stateMachineArn)) {
            throw new IllegalArgumentException("State machine ARN is required");
        }
        this.client = client;
        this.mapper = mapper;
        this.stateMachineArn = stateMachineArn;
    }

    public String start(WorkflowSubmissionAdapter.WorkflowSubmission submission) {
        try {
            String input = mapper.writeValueAsString(submission);
            String name = "submission-" + sha256(submission.submissionId()).substring(0, 48);
            return client.startExecution(StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn).name(name).input(input).build()).executionArn();
        } catch (ExecutionAlreadyExistsException e) {
            // Standard StartExecution is idempotent only while an identical execution
            // remains open. A closed name collision is a conflict, not a new request.
            throw new IllegalStateException("Submission already has a workflow execution", e);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize workflow submission", e);
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
