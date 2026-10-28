package org.sfa.request.stepfunctions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lambda entry point for the ASL firstBatch and nextBatch operations. */
public final class WorkflowBatchTaskHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final WorkflowBatchTaskAdapter batches;

    public WorkflowBatchTaskHandler() {
        this(new WorkflowBatchTaskAdapter(configuredBatchSize()));
    }

    WorkflowBatchTaskHandler(WorkflowBatchTaskAdapter batches) {
        this.batches = batches;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        if (input == null || !(input.get("operation") instanceof String operation)) {
            throw new IllegalArgumentException("Workflow operation is required");
        }
        if (!operation.equals("firstBatch") && !operation.equals("nextBatch")) {
            throw new IllegalArgumentException("Unsupported batch operation");
        }
        Map<?, ?> match = requiredMap(input, "match");
        List<String> candidates = requiredCandidates(match);
        WorkflowBatchTaskAdapter.Batch batch = operation.equals("firstBatch")
                ? batches.firstBatch(candidates) : batches.nextBatch(candidates, previous(input));
        return Map.of("exhausted", batch.exhausted(), "number", batch.number(),
                "candidateIds", batch.candidateIds());
    }

    private static Map<?, ?> requiredMap(Map<String, Object> input, String key) {
        if (!(input.get(key) instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(key + " must be an object");
        }
        return map;
    }

    private static List<String> requiredCandidates(Map<?, ?> match) {
        if (!Boolean.TRUE.equals(match.get("hasCandidates"))
                || !(match.get("candidates") instanceof List<?> raw)) {
            throw new IllegalArgumentException("Matching result must contain candidates");
        }
        List<String> candidates = new ArrayList<>(raw.size());
        for (Object value : raw) {
            if (!(value instanceof String id)) {
                throw new IllegalArgumentException("Candidate IDs must be strings");
            }
            candidates.add(id);
        }
        return candidates;
    }

    private static WorkflowBatchTaskAdapter.Batch previous(Map<String, Object> input) {
        Map<?, ?> data = requiredMap(input, "previous");
        if (!(data.get("number") instanceof Number number)
                || number.intValue() != number.doubleValue()
                || !(data.get("exhausted") instanceof Boolean exhausted)
                || !(data.get("candidateIds") instanceof List<?> raw)) {
            throw new IllegalArgumentException("Invalid previous batch payload");
        }
        List<String> ids = new ArrayList<>(raw.size());
        for (Object value : raw) {
            if (!(value instanceof String id)) {
                throw new IllegalArgumentException("Previous candidate IDs must be strings");
            }
            ids.add(id);
        }
        return new WorkflowBatchTaskAdapter.Batch(exhausted, number.intValue(), ids);
    }

    private static int configuredBatchSize() {
        String value = System.getenv("SAAYAM_WORKFLOW_BATCH_SIZE");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("SAAYAM_WORKFLOW_BATCH_SIZE must be configured");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("SAAYAM_WORKFLOW_BATCH_SIZE must be an integer", e);
        }
    }
}
