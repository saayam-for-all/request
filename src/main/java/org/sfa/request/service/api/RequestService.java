package org.sfa.request.service.api;

import org.sfa.request.dto.AdminRequestDTO;
import org.sfa.request.dto.GetHelpRequestsDTO;
import org.sfa.request.dto.RequestCommentDTO;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestUpdateDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.response.SaayamResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

/**
 * Interface: RequestService
 * Package: org.sfa.request.service.api
 *
 * Provides business operations for handling Requests in the Saayam system.
 *
 * Key Changes (aligned with new schema):
 * - Uses HelpCategory instead of RequestCategory
 * - Uses requestLocation instead of city/zipCode
 * - Adds requestSubject
 * - Uses isLeadVolunteer (enum YES/NO) instead of leadVolunteerUserId
 *
 * Methods:
 * - createRequest: Create a new request for a user
 * - getRequestById: Fetch a single request by ID
 * - getRequests: Fetch paginated list of active requests for a user
 * - updateRequest: Update fields of an existing request
 * - deleteRequest: Mark a request as deleted
 * - cancelRequest: Mark a request as cancelled
 * - resumeRequest: Resume a previously cancelled request
 *
 * Each method returns a SaayamResponse wrapper for consistent API responses.
 *
 * @author Fan Peng
 * @version 1.1
 * Updated: 2025/10/06
 */
public interface RequestService {

    SaayamResponse<Request> createRequest(
            String requesterId,
            RequestDTO requestDTO,
            Locale locale
    );

    SaayamResponse<Request> getRequestById(
            String requesterId,
            String requestId,
            Locale locale
    );

    SaayamResponse<PagedResponse<Request>> getRequests(
            String requesterId,
            Pageable pageable,
            Locale locale
    );

    SaayamResponse<Request> updateRequest(
            RequestUpdateDTO requestUpdateDTO,
            Locale locale
    );

    SaayamResponse<Void> deleteRequest(
            String requesterId,
            String requestId,
            Locale locale
    );

    SaayamResponse<Request> cancelRequest(
            String requesterId,
            String requestId,
            Locale locale
    );

    SaayamResponse<Request> resumeRequest(
            String requesterId,
            String requestId,
            Locale locale
    );

    SaayamResponse<PagedResponse<GetHelpRequestsDTO>> getAllHelpRequests(
            Pageable pageable,
            Locale locale
    );

    SaayamResponse<PagedResponse<GetHelpRequestsDTO>> getUserHelpRequests(
            String userId,
            int page,
            int size,
            Locale locale
    );

    SaayamResponse<PagedResponse<AdminRequestDTO>> getAdminRequests(
            Pageable pageable,
            Locale locale
    );

    RequestCommentDTO addComment(
            String requesterId,
            String requestId,
            RequestCommentDTO requestCommentDTO,
            Locale locale
    );

    List<RequestCommentDTO> getComments(
            String requesterId,
            String requestId,
            Locale locale
    );

    RequestCommentDTO updateComment(
            String requesterId,
            Long id,
            RequestCommentDTO requestCommentDTO,
            Locale locale
    );

    void deleteComment(
            String requesterId,
            Long id,
            Locale locale
    );
}

