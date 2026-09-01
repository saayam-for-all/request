package org.sfa.request.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ClassName: SaayamStatusCode
 * Package: org.sfa.request.constant
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/12 1:32
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum SaayamStatusCode {
    LAMBDA_TIMEOUT("SAAYAM-1000", "Lambda function timed out"),

    // total range: 1400-1599
    // Success codes (1400-1499)
    SUCCESS("SAAYAM-1400", "Operation successful"),
    REQUEST_CREATED("SAAYAM-1401", "Request created"),
    REQUEST_UPDATED("SAAYAM-1402", "Request updated"),
    REQUEST_DELETED("SAAYAM-1403", "Request deleted"),
    REQUEST_CANCELLED("SAAYAM-1404", "Request cancelled"),
    REQUEST_RESUMED("SAAYAM-1405", "Request resumed"),
    REQUESTS_RETRIEVED("SAAYAM-1406", "Requests retrieved successfully"),
    REQUEST_SENT_TO_QUEUE("SAAYAM-1407", "Request sent to queue"),

    // Client error codes (1500-1549)
    BAD_REQUEST("SAAYAM-1500", "Bad request"),
    INVALID_PARAMETER("SAAYAM-1501", "Invalid parameter"),
    REQUEST_NOT_FOUND("SAAYAM-1502", "Request not found"),
    REQUEST_CONFLICT("SAAYAM-1503", "Request conflict"),
    UNAUTHORIZED("SAAYAM-1504", "Unauthorized"),
    FORBIDDEN("SAAYAM-1505", "Forbidden"),
    INVALID_REQUEST_STATUS("SAAYAM-1506", "Invalid request status"),
    INVALID_REQUEST_PRIORITY("SAAYAM-1507", "Invalid request priority"),
    INVALID_REQUEST_TYPE("SAAYAM-1508", "Invalid request type"),
    INVALID_REQUEST_CATEGORY("SAAYAM-1509", "Invalid request category"),
    INVALID_REQUEST_FOR("SAAYAM-1510", "Invalid request for"),
    REQUEST_ALREADY_CANCELLED("SAAYAM-1511", "Request already cancelled"),
    REQUEST_ALREADY_DELETED("SAAYAM-1512", "Request already deleted"),
    ENUM_UNSPECIFIED("SAAYAM-1513", "Enum unspecified"),

    // Server error codes (1550-1599)
    INTERNAL_SERVER_ERROR("SAAYAM-1550", "Internal server error"),
    DATABASE_ERROR("SAAYAM-1551", "Database error"),
    EXTERNAL_SERVICE_ERROR("SAAYAM-1552", "External service error"),
    UNEXPECTED_ERROR("SAAYAM-1553", "Unexpected error occurred");

    private final String code;
    private final String defaultMessage;
}