package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.ExecutionAlreadyExistsException;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowExecutionStarterTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final SfnClient client = mock(SfnClient.class);

    @Test
    void sameSubmissionUsesDeterministicExecutionNameAndEnvelope() throws Exception {
        when(client.startExecution(any(StartExecutionRequest.class)))
                .thenReturn(StartExecutionResponse.builder().executionArn("arn:execution:one").build());
        var input = new WorkflowSubmissionAdapter(mapper).adapt("submission-123", """
                {"requesterId":"SID-100","requestSubject":"Help","requestDescription":"Need help",
                "requestPriority":{"requestPriorityId":1},"helpCategory":{"catId":"4.3.1"},
                "requestFor":{"requestForId":0}}
                """);
        var starter = new WorkflowExecutionStarter(client, mapper, "arn:state-machine:test");
        assertEquals("arn:execution:one", starter.start(input));
        assertEquals("arn:execution:one", starter.start(input));
        var capture = ArgumentCaptor.forClass(StartExecutionRequest.class);
        verify(client, times(2)).startExecution(capture.capture());
        assertEquals(capture.getAllValues().get(0).name(), capture.getAllValues().get(1).name());
        assertEquals(capture.getAllValues().get(0).input(), capture.getAllValues().get(1).input());
        assertEquals("SID-100", mapper.readTree(capture.getValue().input()).path("requestData").path("requesterId").asText());
        assertFalse(capture.getValue().name().contains("SID-100"));
    }

    @Test
    void closedExecutionCollisionIsNotRestarted() {
        when(client.startExecution(any(StartExecutionRequest.class)))
                .thenThrow(ExecutionAlreadyExistsException.builder().message("exists").build());
        var input = new WorkflowSubmissionAdapter(mapper).adapt("submission-123", """
                {"requesterId":"SID-100","requestSubject":"Help","requestDescription":"Need help",
                "requestPriority":{"requestPriorityId":1},"helpCategory":{"catId":"4.3.1"},
                "requestFor":{"requestForId":0}}
                """);
        var starter = new WorkflowExecutionStarter(client, mapper, "arn:state-machine:test");
        assertThrows(IllegalStateException.class, () -> starter.start(input));
        verify(client, times(1)).startExecution(any(StartExecutionRequest.class));
    }
}
