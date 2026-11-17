package org.sfa.request.stepfunctions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.sfa.request.config.SpringContext;

import java.util.Map;

import static org.sfa.request.stepfunctions.Part4WorkStore.Action;

/** Internal Lambda entry point. Only the state machine role should be allowed to invoke it. */
public final class WorkflowFinalizationTaskHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final WorkflowFinalizationTask task;

    public WorkflowFinalizationTaskHandler() {
        this(null);
    }

    WorkflowFinalizationTaskHandler(WorkflowFinalizationTask task) {
        this.task = task;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        if (input == null || !(input.get("operation") instanceof String operation)) {
            throw new IllegalArgumentException("Finalization operation is required");
        }
        Action action = switch (operation) {
            case "addHelpersIfActive" -> Action.ADD_HELPERS;
            case "delegateIfActive" -> Action.DELEGATE;
            case "resolveIfActive" -> Action.COMPLETE;
            case "confirmWorkCancellation" -> Action.CANCEL;
            default -> throw new IllegalArgumentException("Unsupported finalization operation");
        };
        String requestId = requiredId(input, "requestId");
        String decisionId = requiredId(input, "decisionId");
        if (!(input.get("executionId") instanceof String executionId)
                || !WorkflowFinalizationTask.validId(executionId, 512)) {
            throw new IllegalArgumentException("Invalid executionId");
        }
        try {
            WorkflowFinalizationTask finalization = task != null ? task
                    : SpringContext.getContext().getBean(WorkflowFinalizationTask.class);
            return Map.of("outcome", finalization.apply(action, requestId, executionId, decisionId).name());
        } catch (RuntimeException failure) {
            // Lambda errors are visible in execution history. Persistence exceptions may contain user data.
            throw new IllegalStateException("Finalization dependency failed");
        }
    }

    private static String requiredId(Map<String, Object> input, String field) {
        if (!(input.get(field) instanceof String value) || !WorkflowFinalizationTask.validId(value)) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return value;
    }
}
