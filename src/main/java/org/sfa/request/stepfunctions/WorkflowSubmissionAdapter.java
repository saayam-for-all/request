package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.sfa.request.model.enums.RequestTypeEnum;

import java.io.IOException;
import java.util.regex.Pattern;

/** Converts the agreed frontend JSON to the workflow envelope without dropping additional fields. */
public final class WorkflowSubmissionAdapter {
    private static final Pattern KEY = Pattern.compile("[A-Za-z0-9._-]{8,128}");
    private static final int MAX_INPUT_BYTES = 32 * 1024;
    private final ObjectMapper mapper;

    public WorkflowSubmissionAdapter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public WorkflowSubmission adapt(String submissionId, String body) {
        if (submissionId == null || !KEY.matcher(submissionId).matches()) {
            throw new IllegalArgumentException("A stable submission ID of 8-128 safe characters is required");
        }
        if (body == null || body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_INPUT_BYTES) {
            throw new IllegalArgumentException("Submission must be present and no larger than 32 KiB");
        }
        try {
            JsonNode parsed = mapper.readTree(body);
            if (!(parsed instanceof ObjectNode request)) {
                throw new IllegalArgumentException("Submission must be a JSON object");
            }
            requireText(request, "requesterId");
            requireText(request, "requestSubject");
            requireText(request, "requestDescription");
            requireInt(request.path("requestPriority"), "requestPriorityId");
            requireText(request.path("helpCategory"), "catId");
            requireInt(request.path("requestFor"), "requestForId");
            JsonNode type = request.path("requestType");
            if (type.isMissingNode() || type.isNull()) {
                ObjectNode remote = mapper.createObjectNode();
                remote.put("requestTypeId", RequestTypeEnum.REMOTE.getId());
                request.set("requestType", remote);
            } else {
                int typeId = requireInt(type, "requestTypeId");
                if (typeId != RequestTypeEnum.IN_PERSON.getId() && typeId != RequestTypeEnum.REMOTE.getId()) {
                    throw new IllegalArgumentException("Unsupported request type");
                }
            }
            if (!request.hasNonNull("isCalamity")) {
                request.put("isCalamity", false);
            } else if (!request.path("isCalamity").isBoolean()) {
                throw new IllegalArgumentException("isCalamity must be boolean");
            }
            if (!request.hasNonNull("isLeadVolunteer")) {
                request.put("isLeadVolunteer", 0);
            } else if (!request.path("isLeadVolunteer").isIntegralNumber()
                    || (request.path("isLeadVolunteer").asInt() != 0
                    && request.path("isLeadVolunteer").asInt() != 1)) {
                throw new IllegalArgumentException("isLeadVolunteer must be 0 or 1");
            }
            // No raw request content is logged here. The execution history must also
            // have logging disabled or encrypted according to the deployment policy.
            return new WorkflowSubmission(submissionId, request.deepCopy());
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed submission JSON", e);
        }
    }

    private static String requireText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException(field + " must be nonblank text");
        }
        return value.asText();
    }

    private static int requireInt(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isIntegralNumber()) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        return value.asInt();
    }

    public record WorkflowSubmission(String submissionId, JsonNode requestData) { }
}
