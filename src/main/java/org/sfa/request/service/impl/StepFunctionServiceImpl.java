package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.StepFunctionInputDTO;
import org.sfa.request.service.api.StepFunctionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.time.Instant;

/**
 * Implementation of Step Function Service
 * Handles starting Step Function executions when requests are created
 * 
 * @author Saayam Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StepFunctionServiceImpl implements StepFunctionService {

    private final SfnClient sfnClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.stepfunctions.state-machine-arn}")
    private String stateMachineArn;

    @Override
    public String startExecution(StepFunctionInputDTO input) {
        try {
            // Convert input DTO to JSON string
            String inputJson = objectMapper.writeValueAsString(input);
            
            log.info("Starting Step Function execution for request: {}", input.getRequestId());
            log.debug("Step Function input: {}", inputJson);

            // Create execution name with timestamp to ensure uniqueness
            String executionName = String.format("request-%s-%d", 
                input.getRequestId().replace("REQ-", ""), 
                Instant.now().toEpochMilli());

            // Start the Step Function execution
            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn)
                    .name(executionName)
                    .input(inputJson)
                    .build();

            StartExecutionResponse response = sfnClient.startExecution(executionRequest);
            
            log.info("Step Function execution started successfully. Execution ARN: {}", response.executionArn());
            return response.executionArn();

        } catch (Exception e) {
            log.error("Failed to start Step Function execution for request: {}", input.getRequestId(), e);
            throw new RuntimeException("Failed to start Step Function execution: " + e.getMessage(), e);
        }
    }
}
