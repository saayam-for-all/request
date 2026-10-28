package org.sfa.request.stepfunctions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.sfa.request.config.SpringContext;
import org.sfa.request.model.enums.RequestTypeEnum;

import java.util.Map;

/** Lambda entry point for the local lexicon classifier and existing matching queries. */
public class WorkflowReadTaskHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private WorkflowTaskAdapter adapter;

    public WorkflowReadTaskHandler() { }

    WorkflowReadTaskHandler(WorkflowTaskAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        if (input == null || !(input.get("operation") instanceof String operation)) {
            throw new IllegalArgumentException("Workflow operation is required");
        }
        WorkflowTaskAdapter tasks = adapter != null ? adapter : SpringContext.getContext().getBean(WorkflowTaskAdapter.class);
        return switch (operation) {
            case "classify" -> Map.of("code", tasks.classify(text(input, "subject"), text(input, "description")).code());
            case "matchInPerson" -> match(tasks, text(input, "requestId"), RequestTypeEnum.IN_PERSON.getId());
            case "matchRemote" -> match(tasks, text(input, "requestId"), RequestTypeEnum.REMOTE.getId());
            default -> throw new IllegalArgumentException("Unsupported workflow operation");
        };
    }

    private static Map<String, Object> match(WorkflowTaskAdapter tasks, String requestId, int typeId) {
        var result = tasks.match(requestId, typeId);
        return Map.of("hasCandidates", result.hasCandidates(), "candidates", result.candidates());
    }

    private static String text(Map<String, Object> input, String field) {
        Object value = input.get(field);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new IllegalArgumentException(field + " must be nonblank text");
        }
        return string;
    }
}
