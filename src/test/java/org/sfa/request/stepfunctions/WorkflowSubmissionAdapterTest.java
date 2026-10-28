package org.sfa.request.stepfunctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.RequestDTO;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowSubmissionAdapterTest {
    private final WorkflowSubmissionAdapter adapter = new WorkflowSubmissionAdapter(new ObjectMapper());
    private static final String INPUT = """
            {"requesterId":"SID-100","requestSubject":"Calculus help","requestDescription":"Need tutoring",
             "requestPriority":{"requestPriorityId":1},"requestType":{"requestTypeId":1},
             "helpCategory":{"catId":"4.3.1"},"requestFor":{"requestForId":0},
             "additionalFields":{"4.3.1.A":["4.3.1.A.4"]}}
            """;

    @Test
    void preservesFrontendShapeAndAdditionalFields() {
        var submission = adapter.adapt("submission-123", INPUT);
        assertEquals("submission-123", submission.submissionId());
        assertEquals(1, submission.requestData().path("requestType").path("requestTypeId").asInt());
        assertEquals("4.3.1.A.4", submission.requestData().path("additionalFields")
                .path("4.3.1.A").get(0).asText());
        assertFalse(submission.requestData().path("isCalamity").asBoolean());
        assertEquals(0, submission.requestData().path("isLeadVolunteer").asInt());
    }

    @Test
    void usesExistingRemoteDefault() {
        var submission = adapter.adapt("submission-123", INPUT.replace("\"requestType\":{\"requestTypeId\":1},", ""));
        assertEquals(2, submission.requestData().path("requestType").path("requestTypeId").asInt());
    }

    @Test
    void rejectsMalformedOrUnsupportedInputs() {
        assertThrows(IllegalArgumentException.class, () -> adapter.adapt("short", INPUT));
        assertThrows(IllegalArgumentException.class, () -> adapter.adapt("submission-123", "not json"));
        assertThrows(IllegalArgumentException.class, () -> adapter.adapt("submission-123", INPUT.replace("\"requestTypeId\":1", "\"requestTypeId\":0")));
        assertThrows(IllegalArgumentException.class, () -> adapter.adapt("submission-123", INPUT.replace("Calculus help", "")));
    }

    @Test
    void agreedFrontendSubmissionDeserializesWithoutStatusAndRetainsAdditionalFields() throws Exception {
        RequestDTO dto = new ObjectMapper().readValue(INPUT, RequestDTO.class);
        assertNull(dto.getRequestStatus());
        assertEquals("4.3.1.A.4", dto.getAdditionalFields().path("4.3.1.A").get(0).asText());
    }
}
