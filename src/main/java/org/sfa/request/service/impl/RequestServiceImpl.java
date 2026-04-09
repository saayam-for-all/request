package org.sfa.request.service.impl;

import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.GuestDetailsDTO;
import org.sfa.request.dto.ReqAddInfoDTO;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.EnumUnspecifiedException;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.*;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.repository.*;
import org.sfa.request.response.SaayamResponse;
import lombok.RequiredArgsConstructor;
import org.sfa.request.service.api.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
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
    private final MessageSource messageSource;
    private final ReqAddInfoRepository reqAddInfoRepository;
    private final UserRepository userRepository;

    private final S3Client s3Client;
    @Value("${saayam.s3.buckets.usPrivate}")
    private String bucket;

    @Value("${saayam.s3.maxBytes}")
    private long maxBytes;

    @Value("${saayam.s3.allowedMime}")
    private String allowedMimeCsv;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public SaayamResponse<Request> createRequest(String requesterId, RequestDTO requestDTO, Locale locale) {
        validateEnumIds(requestDTO, locale);

        String userTimezone = getUserTimezone(requesterId);
        logger.info("User timezone for {}: {}", requesterId, userTimezone);

        RequestPriority requestPriority = getRequestPriority(requestDTO.getRequestPriority().getRequestPriorityId(), locale);
        RequestType requestType = getRequestType(requestDTO.getRequestType().getRequestTypeId(), locale);
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
        try {
            Request savedRequest = withRetry("insertHelpRequest", 3, () ->
                    requestRepository.save(request)
            );

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

                withRetry("insertGuestDetails", 3, () -> {
                    requestGuestDetailsRepository.save(guestDetails);
                    return null;
                });

            }

            if (requestDTO.getAdditionalFields() != null
                    && !requestDTO.getAdditionalFields().isEmpty()) {
                withRetry("insertAdditionalFields", 3, () -> {
                    insertAdditionalInfo(savedRequest.getRequestId(),
                            requestDTO.getAdditionalFields(),
                            userTimezone);  // ← add this
                    return null;
                });
            }

            logger.info("Created request with ID: {}", savedRequest.getRequestId());
            String message = messageSource.getMessage("success.requestCreated", new Object[]{savedRequest.getRequestId()}, locale);
            return SaayamResponse.success(SaayamStatusCode.REQUEST_CREATED, message, savedRequest);
        } catch (Exception e) {
        logger.error("createRequest failed after all retries: {}", e.getMessage());
        throw new RuntimeException(e.getMessage(), e);
    }
        }

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<Request> getRequestById(String requesterId, String requestId, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);
        logger.info("Retrieved request with ID: {}", requestId);
        String message = messageSource.getMessage("success.requestFound", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, request);
    }

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<PagedResponse<Request>> getRequests(String requesterId, Pageable pageable, Locale locale) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "requestId");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Request> requests = requestRepository.findAllActiveByRequesterId(requesterId, RequestStatusEnum.DELETED.getId(), sortedPageable);

        PagedResponse<Request> pagedResponse = new PagedResponse<>(requests);

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
            requestRepository.save(request);

            logger.info("Deleted request with ID: {}", requestId);
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
        String message = messageSource.getMessage("success.requestResumed", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.REQUEST_RESUMED, message, resumedRequest);
    }

    private void validateEnumIds(RequestDTO requestDTO, Locale locale) {
        validateEnumId(requestDTO.getRequestPriority().getRequestPriorityId(), "RequestPriority", locale);
        validateEnumId(requestDTO.getRequestType().getRequestTypeId(), "RequestType", locale);
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

    private void insertAdditionalInfo(String reqId, Map<String, Object> additionalFields,
                                      String userTimezone) {
        for (Map.Entry<String, Object> entry : additionalFields.entrySet()) {
            String fieldId = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                // list-type field — one row per selected item
                List<String> items = (List<String>) value;
                for (String itemId : items) {
                    if (itemId == null || itemId.isBlank()) continue;
                    reqAddInfoRepository.save(ReqAddInfo.builder()
                            .reqId(reqId)
                            .fieldId(fieldId)
                            .itemId(itemId)
                            .fieldValue(null)
                            .build());
                }

            } else if (value instanceof Map) {
                // date/time field — nested object
                Map<String, String> dateMap = (Map<String, String>) value;
                handleDateRangeField(reqId, fieldId, dateMap, userTimezone);

            } else {
                // string/int/float/currency — one row, itemId is NULL
                String fieldValue = value != null ? value.toString().trim() : null;
                reqAddInfoRepository.save(ReqAddInfo.builder()
                        .reqId(reqId)
                        .fieldId(fieldId)
                        .itemId(null)
                        .fieldValue(fieldValue)
                        .build());
            }
        }
        logger.info("Inserted {} additional fields for reqId: {}",
                additionalFields.size(), reqId);
    }


    private <T> T withRetry(String stepName, int maxAttempts,
                            java.util.concurrent.Callable<T> action) throws Exception {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            try {
                attempts++;
                logger.info("Step '{}' - attempt {}/{}", stepName, attempts, maxAttempts);
                return action.call();
            } catch (Exception e) {
                lastException = e;
                logger.warn("Step '{}' failed on attempt {}/{}: {}",
                        stepName, attempts, maxAttempts, e.getMessage());
                if (attempts < maxAttempts) {
                    Thread.sleep(100L * attempts); // wait 100ms, 200ms, 300ms
                }
            }
        }

        logger.error("Step '{}' failed after {} attempts", stepName, maxAttempts);
        throw lastException;
    }

    private String getUserTimezone(String requesterId) {
        return userRepository.findTimeZoneByUserId(requesterId)
                .orElseGet(() -> {
                    logger.warn("No timezone found for user: {} — defaulting to UTC",
                            requesterId);
                    return "UTC";
                });
    }

    private void handleDateRangeField(String reqId, String fieldId,
                                      Map<String, String> dateMap,
                                      String userTimezone) {
        ZoneId userZone;
        try {
            userZone = ZoneId.of(userTimezone);
        } catch (Exception e) {
            logger.warn("Invalid timezone '{}' — defaulting to UTC", userTimezone);
            userZone = ZoneOffset.UTC;
        }

        for (Map.Entry<String, String> e : dateMap.entrySet()) {
            String slot = e.getKey();    // e.g. "6.1.B.1"
            String value = e.getValue(); // e.g. "2026-03-24T11:37:00"

            if (value == null || value.isBlank()) continue;

            try {
                String utcValue = LocalDateTime.parse(value)
                        .atZone(userZone)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                logger.info("Date field — slot: {}, local: {}, timezone: {}, UTC: {}",
                        slot, value, userTimezone, utcValue);

                reqAddInfoRepository.save(ReqAddInfo.builder()
                        .reqId(reqId)
                        .fieldId(fieldId)
                        .itemId(slot)
                        .fieldValue(utcValue)
                        .build());

            } catch (Exception ex) {
                logger.error("Failed to parse date for slot {}: {}", slot, value);
                throw new InvalidRequestException(
                        "Invalid date format for field " + fieldId +
                                " slot " + slot + ": '" + value +
                                "'. Expected format: '2026-03-24T11:37:00'"
                );
            }
        }
    }

    @Transactional
    public List<String> uploadMultipleAttachments(
            String requesterId,
            String requestId,
            List<MultipartFile> files,
            Locale locale
    ) {
        if (files == null || files.isEmpty()) {
            throw new InvalidRequestException("No files provided");
        }
        Request request = findActiveRequest(requesterId, requestId, locale);
        List<String> existing = parseAttachmentPaths(request.getRequestDocumentLink());
        // count only valid files (present in S3)
        List<String> validExisting = existing.stream()
                .filter(path -> doesFileExist(getKey(path)))
                .toList();
        if (validExisting.size() + files.size() > 5) {
            throw new InvalidRequestException("Maximum 5 attachments allowed");
        }
        List<String> newPaths = new ArrayList<>();
        List<String> responseUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) {
                throw new InvalidRequestException("Invalid file name");
            }
            String cleanFileName = original
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");
            String fileName = requestId + "_" + System.currentTimeMillis() + "_" + cleanFileName;
            String key = "requests/" + requestId + "/helpRequestFiles/" + fileName;
            try {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromBytes(file.getBytes())
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file", e);
            }
            String s3Path = "s3://" + bucket + "/" + key;
            newPaths.add(s3Path);
            responseUrls.add(buildFileUrlFromS3Path(s3Path));
        }
        existing.addAll(newPaths);
        request.setRequestDocumentLink(toJson(existing));
        requestRepository.save(request);
        return responseUrls;
    }
    @Transactional
    public List<Map<String, String>> getAttachments(
            String requesterId,
            String requestId,
            Locale locale
    ) {
        Request request = findActiveRequest(requesterId, requestId, locale);
        List<String> paths = parseAttachmentPaths(request.getRequestDocumentLink());
        List<String> validPaths = new ArrayList<>();
        List<Map<String, String>> response = new ArrayList<>();
        for (String path : paths) {
            String key = getKey(path);
            if (!doesFileExist(key)) {
                continue;
            }
            validPaths.add(path);
            String fileName = key.substring(key.lastIndexOf("/") + 1);
            response.add(Map.of(
                    "fileName", fileName,
                    "url", buildFileUrlFromS3Path(path)
            ));
        }
        // auto clean DB
        if (validPaths.size() != paths.size()) {
            request.setRequestDocumentLink(toJson(validPaths));
            requestRepository.save(request);
        }
        return response;
    }
    @Transactional
    public void deleteAttachment(
            String requesterId,
            String requestId,
            String fileName,
            Locale locale
    ) {
        Request request = findActiveRequest(requesterId, requestId, locale);
        List<String> existingPaths = parseAttachmentPaths(request.getRequestDocumentLink());
        String matchedPath = existingPaths.stream()
                .filter(p -> p.endsWith(fileName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        String key = getKey(matchedPath);
        if (doesFileExist(key)) {
            try {
                s3Client.deleteObject(
                        DeleteObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete file from S3", e);
            }
        }
        // always clean DB
        existingPaths.remove(matchedPath);
        request.setRequestDocumentLink(toJson(existingPaths));
        requestRepository.save(request);
    }
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidRequestException("File size exceeds limit");
        }
        String contentType = file.getContentType();
        List<String> allowed = Arrays.stream(allowedMimeCsv.split(","))
                .map(String::trim)
                .toList();
        if (contentType == null || !allowed.contains(contentType)) {
            throw new InvalidRequestException("Invalid file type");
        }
    }
    private List<String> parseAttachmentPaths(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    private String toJson(List<String> paths) {
        try {
            return objectMapper.writeValueAsString(paths);
        } catch (Exception e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }
    private String buildFileUrlFromS3Path(String s3Path) {
        String key = getKey(s3Path);
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
    private String getKey(String s3Path) {
        return s3Path.replace("s3://" + bucket + "/", "");
    }
    private boolean doesFileExist(String key) {
        try {
            s3Client.headObject(
                    software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
