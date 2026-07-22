package org.sfa.request.service.impl;

import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.GuestDetailsDTO;
import org.sfa.request.dto.RequestSummaryDTO;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.EnumUnspecifiedException;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.*;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.repository.*;
import org.sfa.request.response.SaayamResponse;
import lombok.RequiredArgsConstructor;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.RequestService;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * ClassName: RequestServiceImpl
 * Package: org.sfa.request.service.impl
 * Description:
 *
 * This class implements the RequestService interface, providing business logic
 * for handling various operations related to requests in the Saayam For All system.
 * It manages the creation, retrieval, updating, deletion, cancellation, and resumption
 * of requests. The class utilizes various repositories to interact with the database
 * and applies transactional management to ensure data consistency. It also handles
 * validation of request details, such as priority, type, category, and status.
 *
 * Key functionalities include:
 * - Creating new requests and saving them to the database.
 * - Retrieving existing requests by ID, including both active and deleted ones.
 * - Updating existing requests with new details, while handling specific constraints like
 *   preventing updates to canceled requests.
 * - Deleting requests by marking them as deleted rather than physically removing them
 *   from the database.
 * - Canceling requests, changing their status to 'Cancelled'.
 * - Resuming requests that were previously canceled, reverting their status to 'Created'.
 *
 * This class ensures proper exception handling by throwing specific exceptions
 * like NotFoundException, InvalidRequestException, ConflictException, and EnumUnspecifiedException
 * based on various validation and business logic scenarios. It also utilizes a MessageSource
 * for internationalization, providing localized success and error messages.
 *
 * @author Shariq
 * Create 2025/11/1 23:38
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestServiceImpl.class);

    private final RequestRepository requestRepository;
    private final RequestGuestDetailsRepository requestGuestDetailsRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final RequestIsLeadVolunteerRepository requestIsLeadVolunteerRepository;
    private final RequestPriorityRepository requestPriorityRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final HelpCategoryRepository helpCategoryRepository;
    private final RequestForRepository requestForRepository;
    private final NotificationEventService notificationEventService;
    private final VolunteerAssignmentService volunteerAssignmentService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public SaayamResponse<Request> createRequest(String requesterId, RequestDTO requestDTO, Locale locale) {
        validateEnumIds(requestDTO, locale);

        RequestPriority requestPriority = getRequestPriority(requestDTO.getRequestPriority().getRequestPriorityId(), locale);
        RequestType requestType = getRequestType(resolveRequestTypeId(requestDTO), locale);
        HelpCategory helpCategory = getHelpCategory(requestDTO.getHelpCategory().getCatId(), locale);
        RequestFor requestFor = getRequestFor(requestDTO.getRequestFor().getRequestForId(), locale);
        RequestStatus requestStatus = getRequestStatus(RequestStatusEnum.CREATED.getId(), locale);
        RequestIsLeadVolunteer isLeadVolunteer = getIsLeadVolunteer(requestDTO.getIsLeadVolunteer(), locale);

        Request request = buildRequest(
                requesterId,
                requestDTO,
                requestPriority,
                requestType,
                helpCategory,
                requestFor,
                requestStatus,
                isLeadVolunteer
        );
        Request savedRequest = requestRepository.save(request);

        if (requestFor.getRequestForId() == 1 && requestDTO.getGuestDetails() != null) {
            GuestDetailsDTO guestDTO = requestDTO.getGuestDetails();

            RequestGuestDetails guestDetails = RequestGuestDetails.builder()
                    .requestId(savedRequest.getRequestId())
                    .reqFname(guestDTO.getReqFname())
                    .reqLname(guestDTO.getReqLname())
                    .reqEmail(guestDTO.getReqEmail())
                    .reqPhone(guestDTO.getReqPhone())
                    .reqAge(guestDTO.getReqAge())
                    .reqGender(guestDTO.getReqGender())
                    .reqPrefLang(guestDTO.getReqPrefLang())
                    .build();

            requestGuestDetailsRepository.save(guestDetails);
        }

        logger.info("Created request with ID: {}", savedRequest.getRequestId());
        notificationEventService.enqueueRequestEvent(NotificationEventType.REQUEST_CREATED, savedRequest, locale);
        volunteerAssignmentService.populateAssignments(savedRequest);
        String message = messageSource.getMessage("success.requestCreated", new Object[]{savedRequest.getRequestId()}, locale);
        return SaayamResponse.success(SaayamStatusCode.REQUEST_CREATED, message, savedRequest);
    }

    /**
     * Requests submitted without a request type default to REMOTE rather than
     * being rejected - matches issue #14 ("Change Request to Remote as default").
     */
    private Integer resolveRequestTypeId(RequestDTO requestDTO) {
        return Optional.ofNullable(requestDTO.getRequestType())
                .map(org.sfa.request.dto.RequestTypeDTO::getRequestTypeId)
                .orElse(RequestTypeEnum.REMOTE.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<Request> getRequestById(String requesterId, String requestId, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);
        volunteerAssignmentService.populateAssignments(request);
        logger.info("Retrieved request with ID: {}", requestId);
        String message = messageSource.getMessage("success.requestFound", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, request);
    }

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<PagedResponse<RequestSummaryDTO>> getRequests(String requesterId, Pageable pageable, Locale locale) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "requestId");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Request> requests = requestRepository.findAllActiveByRequesterId(requesterId, RequestStatusEnum.DELETED.getId(), sortedPageable);
        Page<RequestSummaryDTO> summaries = requests.map(RequestSummaryDTO::fromEntity);

        PagedResponse<RequestSummaryDTO> pagedResponse = new PagedResponse<>(summaries);

        logger.info("Retrieved {} requests for requester ID: {}", requests.getContent().size(), requesterId);
        String message = messageSource.getMessage("success.requestsRetrieved", null, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, pagedResponse);
    }

    @Override
    @Transactional
    public SaayamResponse<Request> updateRequest(String requesterId, String requestId, RequestDTO requestDTO, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);

        if (request.getRequestStatus().getRequestStatusId() == RequestStatusEnum.CANCELLED.getId()) {
            throw new InvalidRequestException(
                    messageSource.getMessage("error.updateCancelledRequest", new Object[]{requestId}, locale)
            );
        }

        updateRequestFields(request, requestDTO, locale);
        request.setLastUpdatedAt(ZonedDateTime.now());
        Request updatedRequest = requestRepository.save(request);

        logger.info("Updated request with ID: {}", requestId);
        notificationEventService.enqueueRequestEvent(NotificationEventType.REQUEST_UPDATED, updatedRequest, locale);
        volunteerAssignmentService.populateAssignments(updatedRequest);
        String message = messageSource.getMessage("success.requestUpdated", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.REQUEST_UPDATED, message, updatedRequest);
    }

    @Override
    @Transactional
    public SaayamResponse<Void> deleteRequest(String requesterId, String requestId, Locale locale) {
        Request request = findRequestIncludingDeleted(requesterId, requestId, locale);

        if (request.getRequestStatus().getRequestStatusId() != RequestStatusEnum.DELETED.getId()) {
            RequestStatus deletedStatus = getRequestStatus(RequestStatusEnum.DELETED.getId(), locale);
            request.setRequestStatus(deletedStatus);
            request.setLastUpdatedAt(ZonedDateTime.now());
            Request deletedRequest = requestRepository.save(request);

            logger.info("Deleted request with ID: {}", requestId);
            notificationEventService.enqueueRequestEvent(NotificationEventType.REQUEST_DELETED, deletedRequest, locale);
            String message = messageSource.getMessage("success.requestDeleted", new Object[]{requestId}, locale);
            return SaayamResponse.success(SaayamStatusCode.REQUEST_DELETED, message, null);
        } else {
            throw new ConflictException(
                    messageSource.getMessage("error.requestAlreadyDeleted", new Object[]{requestId}, locale));
        }
    }

    @Override
    @Transactional
    public SaayamResponse<Request> cancelRequest(String requesterId, String requestId, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);

        if (request.getRequestStatus().getRequestStatusId() == RequestStatusEnum.CANCELLED.getId()) {
            throw new InvalidRequestException(
                    messageSource.getMessage("error.requestAlreadyCancelled", new Object[]{requestId}, locale)
            );
        }

        RequestStatus cancelledStatus = getRequestStatus(RequestStatusEnum.CANCELLED.getId(), locale);
        request.setRequestStatus(cancelledStatus);
        request.setLastUpdatedAt(ZonedDateTime.now());
        Request cancelledRequest = requestRepository.save(request);

        logger.info("Cancelled request with ID: {}", requestId);
        notificationEventService.enqueueRequestEvent(NotificationEventType.REQUEST_CANCELLED, cancelledRequest, locale);
        volunteerAssignmentService.populateAssignments(cancelledRequest);
        String message = messageSource.getMessage("success.requestCancelled", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.REQUEST_CANCELLED, message, cancelledRequest);
    }

    @Override
    @Transactional
    public SaayamResponse<Request> resumeRequest(String requesterId, String requestId, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);

        if (request.getRequestStatus().getRequestStatusId() != RequestStatusEnum.CANCELLED.getId()) {
            throw new InvalidRequestException(
                    messageSource.getMessage("error.requestNotCancelled", new Object[]{requestId}, locale)
            );
        }

        RequestStatus createdStatus = getRequestStatus(RequestStatusEnum.CREATED.getId(), locale);
        request.setRequestStatus(createdStatus);
        request.setLastUpdatedAt(ZonedDateTime.now());
        Request resumedRequest = requestRepository.save(request);

        logger.info("Resumed request with ID: {}", requestId);
        notificationEventService.enqueueRequestEvent(NotificationEventType.REQUEST_RESUMED, resumedRequest, locale);
        volunteerAssignmentService.populateAssignments(resumedRequest);
        String message = messageSource.getMessage("success.requestResumed", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.REQUEST_RESUMED, message, resumedRequest);
    }

    private void validateEnumIds(RequestDTO requestDTO, Locale locale) {
        validateEnumId(requestDTO.getRequestPriority().getRequestPriorityId(), "RequestPriority", locale);
        // requestType is optional - defaults to REMOTE via resolveRequestTypeId when absent
        if (requestDTO.getRequestType() != null) {
            validateEnumId(requestDTO.getRequestType().getRequestTypeId(), "RequestType", locale);
        }
        validateEnumId(requestDTO.getHelpCategory().getCatId(), "HelpCategory", locale); // ✅
        validateEnumId(requestDTO.getRequestFor().getRequestForId(), "RequestFor", locale);
    }

    private RequestIsLeadVolunteer getIsLeadVolunteer(Integer id, Locale locale) {
        return requestIsLeadVolunteerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidIsLeadVolunteer", new Object[]{id}, locale)
                ));
    }


    private void validateEnumId(Object id, String enumType, Locale locale) {
        if (id == null) {
            throw new EnumUnspecifiedException(
                    messageSource.getMessage("error.enumUnspecified", new Object[]{enumType}, locale)
            );
        }
    }

    private RequestPriority getRequestPriority(Integer id, Locale locale) {
        return requestPriorityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidRequestPriority", new Object[]{id}, locale)
                ));
    }

    private RequestType getRequestType(Integer id, Locale locale) {
        return requestTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidRequestType", new Object[]{id}, locale)
                ));
    }

    private HelpCategory getHelpCategory(String id, Locale locale) {
        return helpCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidHelpCategory", new Object[]{id}, locale)
                ));
    }

    private RequestFor getRequestFor(Integer id, Locale locale) {
        return requestForRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidRequestFor", new Object[]{id}, locale)
                ));
    }

    private RequestStatus getRequestStatus(Integer id, Locale locale) {
        return requestStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.invalidRequestStatus", new Object[]{id}, locale)
                ));
    }

    private Request findActiveRequest(String requesterId, String requestId, Locale locale) {
        return requestRepository.findActiveByRequestIdAndRequesterId(requestId, requesterId, RequestStatusEnum.DELETED.getId())
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.requestNotFound", new Object[]{requestId, requesterId}, locale)
                ));
    }

    private Request findRequestIncludingDeleted(String requesterId, String requestId, Locale locale) {
        return requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(requestId, requesterId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.requestNotFound", new Object[]{requestId, requesterId}, locale)
                ));
    }

    private Request buildRequest(
            String requesterId,
            RequestDTO requestDTO,
            RequestPriority requestPriority,
            RequestType requestType,
            HelpCategory helpCategory,
            RequestFor requestFor,
            RequestStatus requestStatus,
            RequestIsLeadVolunteer isLeadVolunteer
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return Request.builder()
                .requesterId(requesterId)
                .requestStatus(requestStatus)
                .requestPriority(requestPriority)
                .requestType(requestType)
                .helpCategory(helpCategory)
                .requestFor(requestFor)
                .isLeadVolunteer(isLeadVolunteer)
                .requestLocation(requestDTO.getRequestLocation())
                .requestSubject(requestDTO.getRequestSubject())
                .requestDescription(requestDTO.getRequestDescription())
                .audioRequestDescription(requestDTO.getAudioRequestDescription())
                .isCalamity(requestDTO.getIsCalamity())
                .requestDocumentLink(requestDTO.getRequestDocumentLink())
                .submittedAt(now)
                .servicedAt(requestDTO.getServicedAt())
                .lastUpdatedAt(now)

                .build();
    }

    private void updateRequestFields(Request request, RequestDTO requestDTO, Locale locale) {
        Optional.ofNullable(requestDTO.getRequestPriority())
                .ifPresent(priority -> request.setRequestPriority(getRequestPriority(priority.getRequestPriorityId(), locale)));

        Optional.ofNullable(requestDTO.getRequestType())
                .ifPresent(type -> request.setRequestType(getRequestType(type.getRequestTypeId(), locale)));

        Optional.ofNullable(requestDTO.getHelpCategory())
                .ifPresent(cat -> request.setHelpCategory(getHelpCategory(cat.getCatId(), locale)));

        Optional.ofNullable(requestDTO.getRequestFor())
                .ifPresent(requestFor -> request.setRequestFor(getRequestFor(requestFor.getRequestForId(), locale)));

        Optional.ofNullable(requestDTO.getRequestLocation()).ifPresent(request::setRequestLocation);
        Optional.ofNullable(requestDTO.getRequestSubject()).ifPresent(request::setRequestSubject);

        Optional.ofNullable(requestDTO.getRequestDescription()).ifPresent(request::setRequestDescription);
        Optional.ofNullable(requestDTO.getIsCalamity()).ifPresent(request::setIsCalamity);
        Optional.ofNullable(requestDTO.getRequestDocumentLink()).ifPresent(request::setRequestDocumentLink);

        Optional.ofNullable(requestDTO.getAudioRequestDescription()).ifPresent(request::setAudioRequestDescription);

        Optional.ofNullable(requestDTO.getIsLeadVolunteer())
                .ifPresent(volId -> request.setIsLeadVolunteer(getIsLeadVolunteer(volId, locale)));

        Optional.ofNullable(requestDTO.getServicedAt()).ifPresent(request::setServicedAt);
    }
}
