package org.sfa.request.service.api;

import org.sfa.request.dto.VolunteerAssignmentDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.SaayamResponse;

import java.util.List;
import java.util.Locale;

public interface VolunteerAssignmentService {

    SaayamResponse<Request> assignVolunteer(String requesterId, String requestId, VolunteerAssignmentDTO assignmentDTO, Locale locale);

    SaayamResponse<Void> removeVolunteer(String requesterId, String requestId, String volunteerId, Locale locale);

    /** Adds previously authorized helpers with the request row locked by the workflow transaction. */
    boolean addMissingWorkflowHelpers(String requestId, List<String> volunteerIds);

    /**
     * Populates the transient leadVolunteerUserId / helpingVolunteerUserIds fields on a
     * Request from the volunteers_assigned table. Shared by RequestService so every
     * request response reflects current assignments without duplicating this query.
     */
    void populateAssignments(Request request);
}
