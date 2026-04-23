package org.sfa.request.service.api;

import org.sfa.request.dto.HelperVolunteerDTO;
import org.sfa.request.response.SaayamResponse;

import java.util.List;
import java.util.Locale;

public interface VolunteerService {
    SaayamResponse<List<HelperVolunteerDTO>> getHelpers(String requesterId, String requestId, Locale locale);
    SaayamResponse<HelperVolunteerDTO> addHelper(String requesterId, String requestId, HelperVolunteerDTO dto, Integer actorUserId, Locale locale);
    SaayamResponse<HelperVolunteerDTO> updateHelper(String requesterId, String requestId, String volunteerUserId, HelperVolunteerDTO dto, Integer actorUserId, Locale locale);
    SaayamResponse<Void> removeHelper(String requesterId, String requestId, String volunteerUserId, Integer actorUserId, Locale locale);
}
