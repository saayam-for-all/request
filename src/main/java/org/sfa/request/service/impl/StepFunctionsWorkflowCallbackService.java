package org.sfa.request.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.config.ObjectMapperConfig;
import org.sfa.request.service.api.WorkflowCallbackService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.InvalidTokenException;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;
import software.amazon.awssdk.services.sfn.model.TaskDoesNotExistException;
import software.amazon.awssdk.services.sfn.model.TaskTimedOutException;

import java.util.Map;

/**
 * {@link WorkflowCallbackService} backed by AWS Step Functions.
 *
 * <p>An expired, already-used or unknown token returns false rather than throwing: for
 * callers such as superseding an old verification, "nothing left to resume" is a normal
 * outcome. Other AWS errors (throttling, permissions) propagate.</p>
 */
@Slf4j
@Service
public class StepFunctionsWorkflowCallbackService implements WorkflowCallbackService {

    private final SfnClient sfnClient;
    private final ObjectMapper objectMapper;

    public StepFunctionsWorkflowCallbackService(SfnClient sfnClient) {
        this(sfnClient, ObjectMapperConfig.getObjectMapper());
    }

    StepFunctionsWorkflowCallbackService(SfnClient sfnClient, ObjectMapper objectMapper) {
        this.sfnClient = sfnClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean sendSuccess(String taskToken, Map<String, Object> output) {
        String json;
        try {
            json = objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Workflow output is not serializable", e);
        }

        try {
            sfnClient.sendTaskSuccess(SendTaskSuccessRequest.builder()
                    .taskToken(taskToken)
                    .output(json)
                    .build());
            return true;
        } catch (TaskTimedOutException | TaskDoesNotExistException | InvalidTokenException e) {
            // Never log the token itself: it is a credential that resumes the execution.
            log.info("Workflow task token is no longer valid ({}); nothing to resume", e.getClass().getSimpleName());
            return false;
        }
    }
}
