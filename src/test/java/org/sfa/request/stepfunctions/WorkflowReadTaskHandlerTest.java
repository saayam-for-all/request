package org.sfa.request.stepfunctions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowReadTaskHandlerTest {
    private final WorkflowTaskAdapter adapter = mock(WorkflowTaskAdapter.class);
    private final WorkflowReadTaskHandler handler = new WorkflowReadTaskHandler(adapter);

    @Test
    void classifiesWithExistingServiceAdapter() {
        when(adapter.classify("subject", "description")).thenReturn(new WorkflowTaskAdapter.Classification(2));
        assertEquals(Map.of("code", 2), handler.handleRequest(Map.of(
                "operation", "classify", "subject", "subject", "description", "description"), null));
    }

    @Test
    void routesExpectedTypeAndReturnsCandidateIds() {
        when(adapter.match("REQ-1", 1)).thenReturn(new WorkflowTaskAdapter.MatchResult(true, List.of("VOL-1")));
        assertEquals(Map.of("hasCandidates", true, "candidates", List.of("VOL-1")),
                handler.handleRequest(Map.of("operation", "matchInPerson", "requestId", "REQ-1"), null));
        verify(adapter).match("REQ-1", 1);
    }

    @Test
    void rejectsUnknownOperationWithoutCallingBusinessService() {
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of("operation", "persistAccepted"), null));
        verifyNoInteractions(adapter);
    }
}
