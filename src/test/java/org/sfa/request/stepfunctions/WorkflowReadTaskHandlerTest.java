package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
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
        when(adapter.match("REQ-1", 1, "SID-1", "CAT-1", false))
                .thenReturn(new WorkflowTaskAdapter.MatchResult(true, List.of("VOL-1")));
        assertEquals(Map.of("hasCandidates", true, "candidates", List.of("VOL-1")),
                handler.handleRequest(Map.of("operation", "matchInPerson", "requestId", "REQ-1",
                        "requesterId", "SID-1", "categoryId", "CAT-1", "isCalamity", false), null));
        verify(adapter).match("REQ-1", 1, "SID-1", "CAT-1", false);
    }

    @Test
    void rejectsUnknownOperationWithoutCallingBusinessService() {
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of("operation", "persistAccepted"), null));
        verifyNoInteractions(adapter);
    }

    @Test
    void aslIncludesAllMatchGuardsRequiredByHandler() throws Exception {
        try (InputStream source = getClass().getResourceAsStream("/stepfunctions/request-workflow.asl.json")) {
            assertNotNull(source);
            var states = new ObjectMapper().readTree(source).path("States");
            for (String name : List.of("MatchInPerson", "MatchRemote")) {
                var payload = states.path(name).path("Parameters").path("Payload");
                for (String field : List.of("requestId.$", "requesterId.$", "categoryId.$", "isCalamity.$")) {
                    assertTrue(payload.hasNonNull(field), name + " is missing " + field);
                }
            }
        }
    }
}
