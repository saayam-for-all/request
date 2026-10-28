package org.sfa.request.stepfunctions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Pure, retry-safe selection of at most three disjoint volunteer batches. */
public final class WorkflowBatchTaskAdapter {
    private static final int MAX_BATCHES = 3;
    private final int batchSize;

    public WorkflowBatchTaskAdapter(int batchSize) {
        if (batchSize < 1 || batchSize > 100) {
            throw new IllegalArgumentException("Batch size must be between 1 and 100");
        }
        this.batchSize = batchSize;
    }

    public Batch firstBatch(List<String> candidates) {
        return select(candidates, 1);
    }

    public Batch nextBatch(List<String> candidates, Batch previous) {
        if (previous == null || previous.exhausted() || previous.number() < 1
                || previous.number() > MAX_BATCHES) {
            throw new IllegalArgumentException("Invalid previous batch");
        }
        Batch expected = select(candidates, previous.number());
        if (expected.exhausted() || !expected.candidateIds().equals(previous.candidateIds())) {
            throw new IllegalArgumentException("Previous batch does not match the candidate list");
        }
        return select(candidates, previous.number() + 1);
    }

    private Batch select(List<String> candidates, int number) {
        List<String> distinct = normalize(candidates);
        if (number > MAX_BATCHES || (long) (number - 1) * batchSize >= distinct.size()) {
            return new Batch(true, number, List.of());
        }
        int from = (number - 1) * batchSize;
        int to = Math.min(from + batchSize, distinct.size());
        return new Batch(false, number, List.copyOf(distinct.subList(from, to)));
    }

    private static List<String> normalize(List<String> candidates) {
        if (candidates == null || candidates.isEmpty() || candidates.size() > 1000) {
            throw new IllegalArgumentException("Candidate list must contain 1 to 1000 volunteers");
        }
        LinkedHashSet<String> distinct = new LinkedHashSet<>();
        for (String id : candidates) {
            if (id == null || id.isBlank() || id.length() > 255) {
                throw new IllegalArgumentException("Invalid volunteer ID");
            }
            distinct.add(id);
        }
        return new ArrayList<>(distinct);
    }

    public record Batch(boolean exhausted, int number, List<String> candidateIds) { }
}
