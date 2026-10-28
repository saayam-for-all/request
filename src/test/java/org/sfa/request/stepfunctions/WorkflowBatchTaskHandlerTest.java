package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowBatchTaskHandlerTest {
    private final WorkflowBatchTaskHandler handler = new WorkflowBatchTaskHandler(
            new WorkflowBatchTaskAdapter(2));
    private final Map<String, Object> match = Map.of("hasCandidates", true,
            "candidates", List.of("VOL-1", "VOL-2", "VOL-1", "VOL-3", "VOL-4", "VOL-5", "VOL-6", "VOL-7"));

    @Test
    void handlerReturnsDisjointBatchesAndCapsAtThree() {
        Map<String, Object> first = invoke("firstBatch", null);
        Map<String, Object> second = invoke("nextBatch", first);
        Map<String, Object> third = invoke("nextBatch", second);
        Map<String, Object> exhausted = invoke("nextBatch", third);

        assertEquals(List.of("VOL-1", "VOL-2"), first.get("candidateIds"));
        assertEquals(List.of("VOL-3", "VOL-4"), second.get("candidateIds"));
        assertEquals(List.of("VOL-5", "VOL-6"), third.get("candidateIds"));
        assertEquals(Map.of("exhausted", true, "number", 4, "candidateIds", List.of()), exhausted);
        assertEquals(first, invoke("firstBatch", null));
        assertEquals(second, invoke("nextBatch", first));
    }

    @Test
    void exhaustedWhenNoMoreCandidatesRemain() {
        WorkflowBatchTaskHandler one = new WorkflowBatchTaskHandler(new WorkflowBatchTaskAdapter(2));
        Map<String, Object> shortMatch = Map.of("hasCandidates", true, "candidates", List.of("VOL-1"));
        Map<String, Object> first = one.handleRequest(Map.of("operation", "firstBatch", "match", shortMatch), null);
        Map<String, Object> next = one.handleRequest(Map.of("operation", "nextBatch", "match", shortMatch,
                "previous", first), null);
        assertEquals(Map.of("exhausted", true, "number", 2, "candidateIds", List.of()), next);
    }

    @Test
    void rejectsTamperedPreviousBatchAndMalformedCandidates() {
        assertThrows(IllegalArgumentException.class, () -> invoke("nextBatch",
                Map.of("exhausted", false, "number", 1, "candidateIds", List.of("VOL-X"))));
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of(
                "operation", "firstBatch", "match", Map.of("hasCandidates", true,
                        "candidates", List.of(123))), null));
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of(
                "operation", "firstBatch", "match", Map.of("hasCandidates", false,
                        "candidates", List.of("VOL-1"))), null));
        assertThrows(IllegalArgumentException.class,
                () -> handler.handleRequest(Map.of("operation", "issueOtp"), null));
        assertThrows(IllegalArgumentException.class, () -> new WorkflowBatchTaskAdapter(0));
    }

    @Test
    void aslInvokesHandlerWithTheDocumentedBatchPayload() throws Exception {
        try (InputStream source = getClass().getResourceAsStream("/stepfunctions/request-workflow.asl.json")) {
            assertNotNull(source);
            var states = new ObjectMapper().readTree(source).path("States");
            var first = states.path("PrepareFirstBatch").path("Parameters").path("Payload");
            var next = states.path("PrepareNextBatch").path("Parameters").path("Payload");
            assertEquals("firstBatch", first.path("operation").asText());
            assertEquals("$.matchInvoke.Payload", first.path("match.$").asText());
            assertEquals("nextBatch", next.path("operation").asText());
            assertEquals("$.batchInvoke.Payload", next.path("previous.$").asText());
            assertEquals("$.matchInvoke.Payload", next.path("match.$").asText());
        }
    }

    private Map<String, Object> invoke(String operation, Map<String, Object> previous) {
        if (previous == null) {
            return handler.handleRequest(Map.of("operation", operation, "match", match), null);
        }
        return handler.handleRequest(Map.of("operation", operation, "match", match,
                "previous", previous), null);
    }
}
