package org.sfa.request.service.api;

import org.sfa.request.response.PagedResponse;
import org.sfa.request.model.entity.Request;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.response.SaayamResponse;

import org.springframework.data.domain.Pageable;

import java.util.Locale;

/**
 * ClassName: RequestService
 * Package: org.sfa.request.service.api
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/14 23:32
 * @version 1.0
 */
public interface RequestService {
    SaayamResponse<Request> createRequest(String requesterId, RequestDTO requestDTO, Locale locale);
    SaayamResponse<Request> getRequestById(String requesterId, String requestId, Locale locale);
    SaayamResponse<PagedResponse<Request>> getRequests(String requesterId, Pageable pageable, Locale locale);
    SaayamResponse<Request> updateRequest(String requesterId, String requestId, RequestDTO requestDTO, Locale locale);
    SaayamResponse<Void> deleteRequest(String requesterId, String requestId, Locale locale);
    SaayamResponse<Request> cancelRequest(String requesterId, String requestId, Locale locale);
    SaayamResponse<Request> resumeRequest(String requesterId, String requestId, Locale locale);
}
