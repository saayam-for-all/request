package org.sfa.request.service.api;

import org.sfa.request.dto.HelperVolunteerDTO;
import org.sfa.request.response.SaayamResponse;

import java.util.List;
import java.util.Locale;

public interface VolunteerService {
    SaayamResponse<List<HelperVolunteerDTO>> getHelpers(String requesterId, String requestId, Locale locale);
    SaayamResponse<HelperVolunteerDTO> addHelper(String requesterId, String requestId, HelperVolunteerDTO dto, Locale locale);
    SaayamResponse<Void> removeHelper(String requesterId, String requestId, String volunteerUserId, Locale locale);
}
