package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.InvalidTokenException;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;
import software.amazon.awssdk.services.sfn.model.SfnException;
import software.amazon.awssdk.services.sfn.model.TaskDoesNotExistException;
import software.amazon.awssdk.services.sfn.model.TaskTimedOutException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StepFunctionsWorkflowCallbackServiceTest {

    private final SfnClient sfnClient = mock(SfnClient.class);
    private final StepFunctionsWorkflowCallbackService service =
            new StepFunctionsWorkflowCallbackService(sfnClient, new ObjectMapper());

    @Test
    void sendsTaskSuccessWithJsonOutput() throws Exception {
        assertTrue(service.sendSuccess("token-1", Map.of("result", "SUPERSEDED")));

        ArgumentCaptor<SendTaskSuccessRequest> captor = ArgumentCaptor.forClass(SendTaskSuccessRequest.class);
        verify(sfnClient).sendTaskSuccess(captor.capture());
        assertEquals("token-1", captor.getValue().taskToken());
        assertEquals(Map.of("result", "SUPERSEDED"),
                new ObjectMapper().readValue(captor.getValue().output(), Map.class));
    }

    @Test
    void timedOutTokenReturnsFalse() {
        when(sfnClient.sendTaskSuccess(any(SendTaskSuccessRequest.class)))
                .thenThrow(TaskTimedOutException.builder().message("timed out").build());

        assertFalse(service.sendSuccess("token-1", Map.of()));
    }

    @Test
    void unknownTaskReturnsFalse() {
        when(sfnClient.sendTaskSuccess(any(SendTaskSuccessRequest.class)))
                .thenThrow(TaskDoesNotExistException.builder().message("gone").build());

        assertFalse(service.sendSuccess("token-1", Map.of()));
    }

    @Test
    void invalidTokenReturnsFalse() {
        when(sfnClient.sendTaskSuccess(any(SendTaskSuccessRequest.class)))
                .thenThrow(InvalidTokenException.builder().message("bad token").build());

        assertFalse(service.sendSuccess("token-1", Map.of()));
    }

    @Test
    void otherAwsErrorsPropagate() {
        AwsServiceException throttled = SfnException.builder().message("throttled").statusCode(429).build();
        when(sfnClient.sendTaskSuccess(any(SendTaskSuccessRequest.class))).thenThrow(throttled);

        assertThrows(SfnException.class, () -> service.sendSuccess("token-1", Map.of()));
    }
}
