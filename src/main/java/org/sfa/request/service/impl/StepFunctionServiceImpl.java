package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sfa.request.dto.StepFunctionInputDTO;
import org.sfa.request.service.api.StepFunctionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

@Service
@RequiredArgsConstructor
public class StepFunctionServiceImpl
        implements StepFunctionService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    StepFunctionServiceImpl.class
            );

    private final SfnClient sfnClient;
    private final ObjectMapper objectMapper;

    @Value("${volunteer-matching.step-function.enabled:false}")
    private boolean stepFunctionEnabled;

    @Value("${volunteer-matching.step-function.state-machine-arn:}")
    private String stateMachineArn;

    @Override
    public String startExecution(
            StepFunctionInputDTO input) {

        if (!stepFunctionEnabled) {
            logger.info(
                    "Volunteer matching Step Function disabled for requestId={}",
                    input.getRequestId()
            );

            return null;
        }

        if (stateMachineArn == null
                || stateMachineArn.isBlank()) {

            logger.warn(
                    "Volunteer matching state machine ARN is missing"
            );

            return null;
        }

        try {

            String inputJson =
                    objectMapper.writeValueAsString(input);

            String safeRequestId =
                    input.getRequestId() == null
                            ? "unknown-request"
                            : input.getRequestId()
                            .replaceAll(
                                    "[^A-Za-z0-9-_]",
                                    "-"
                            );

            String executionName =
                    safeRequestId
                            + "-"
                            + System.currentTimeMillis();

            StartExecutionRequest executionRequest =
                    StartExecutionRequest.builder()
                            .stateMachineArn(stateMachineArn)
                            .name(executionName)
                            .input(inputJson)
                            .build();

            StartExecutionResponse response =
                    sfnClient.startExecution(
                            executionRequest
                    );

            return response.executionArn();

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to start volunteer matching workflow",
                    ex
            );
        }
    }
}