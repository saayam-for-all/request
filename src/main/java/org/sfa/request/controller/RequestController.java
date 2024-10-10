package org.sfa.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sfa.request.dto.CommentDTO;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.model.entity.Request;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.service.api.RequestService;
import org.sfa.request.response.SaayamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * ClassName: RequestController
 * Package: org.sfa.request.controller
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/15 1:51
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/api/v1.0.0/requests/{requesterId}")
@RequiredArgsConstructor
@Tag(name = "Request", description = "Request management APIs")
public class RequestController {

    private final RequestService requestService;
    private final LocaleResolver localeResolver;

    @Operation(
            summary = "Create a new request",
            description = "Creates a new request in the Saayam system for the specified requester. " +
                    "The request includes details such as priority, type, category, and description."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Request successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SaayamResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                  "success": true,
                  "statusCode": 201,
                  "saayamCode": "SAAAYAM-1201",
                  "message": "Request REQ-00-000-000-0017 has been successfully created and saved in the system",
                  "data": {
                    "requestId": "REQ-00-000-000-0017",
                    "requesterId": "SID-00-000-000-0001",
                    "requestStatus": {
                      "requestStatusId": 1,
                      "status": "CREATED",
                      "description": "Request has been created",
                      "lastUpdatedAt": "2024-07-15T19:37:44.653792Z"
                    },
                    "requestPriority": {
                      "priorityId": 1,
                      "priority": "LOW",
                      "description": "Low priority",
                      "lastUpdatedAt": "2024-07-15T19:37:44.65499Z"
                    },
                    "requestType": {
                      "requestTypeId": 1,
                      "type": "IN_PERSON",
                      "description": "In-person request",
                      "lastUpdatedAt": "2024-07-15T19:37:44.65581Z"
                    },
                    "requestCategory": {
                      "requestCategoryId": 1,
                      "category": "TECHNICAL_SUPPORT",
                      "description": "Technical support request",
                      "lastUpdatedAt": "2024-07-15T19:37:44.656694Z"
                    },
                    "requestFor": {
                      "requestForId": 1,
                      "description": "Request for self",
                      "lastUpdatedAt": "2024-07-15T19:37:44.657513Z",
                      "for": "SELF"
                    },
                    "city": "MD",
                    "zipCode": "2288",
                    "requestDescription": "Need technical support",
                    "audioRequestDescription": "Audio description of the request",
                    "submittedAt": "2024-07-16T23:03:21.4388422-04:00",
                    "leadVolunteerUserId": 123,
                    "servicedAt": null,
                    "lastUpdatedAt": "2024-07-16T23:03:21.4388422-04:00"
                  },
                  "timestamp": "2024-07-16T23:03:21.471842-04:00"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SaayamResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                  "success": false,
                  "statusCode": 400,
                  "saayamCode": "SAAAYAM-1413",
                  "message": "Invalid value: Unspecified or null value is not allowed for RequestPriority. Please provide a valid option",
                  "timestamp": "2024-07-17T19:02:55.2930539-04:00"
                }
                """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<SaayamResponse<Request>> createRequest(
            @Parameter(description = "Unique identifier of the requester", required = true, example = "SID-00-000-000-0001")
            @PathVariable @NotNull String requesterId,

            @Parameter(description = "Request details", required = true)
            @RequestBody @Valid RequestDTO requestDTO,

            HttpServletRequest request
    ) {
        requestDTO.setRequesterId(requesterId);
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Request> response = requestService.createRequest(requesterId, requestDTO, locale);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<SaayamResponse<Request>> getRequestById(
            @PathVariable @NotNull String requesterId,
            @PathVariable @NotNull String requestId,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Request> response = requestService.getRequestById(requesterId, requestId, locale);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SaayamResponse<PagedResponse<Request>>> getRequests(
            @PathVariable @NotNull String requesterId,
            Pageable pageable,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<PagedResponse<Request>> response = requestService.getRequests(requesterId, pageable, locale);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<SaayamResponse<Request>> updateRequest(
            @PathVariable @NotNull String requesterId,
            @PathVariable @NotNull String requestId,
            @RequestBody @Valid RequestDTO requestDTO,
            HttpServletRequest request
    ) {
        requestDTO.setRequesterId(requesterId);
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Request> response = requestService.updateRequest(requesterId, requestId, requestDTO, locale);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<SaayamResponse<Void>> deleteRequest(
            @PathVariable @NotNull String requesterId,
            @PathVariable @NotNull String requestId,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Void> response = requestService.deleteRequest(requesterId, requestId, locale);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<SaayamResponse<Request>> cancelRequest(
            @PathVariable @NotNull String requesterId,
            @PathVariable @NotNull String requestId,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Request> response = requestService.cancelRequest(requesterId, requestId, locale);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/resume")
    public ResponseEntity<SaayamResponse<Request>> resumeRequest(
            @PathVariable @NotNull String requesterId,
            @PathVariable @NotNull String requestId,
            HttpServletRequest request
    ) {
        Locale locale = localeResolver.resolveLocale(request);
        SaayamResponse<Request> response = requestService.resumeRequest(requesterId, requestId, locale);
        return ResponseEntity.ok(response);
    }


}