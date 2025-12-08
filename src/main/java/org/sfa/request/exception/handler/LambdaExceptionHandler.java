package org.sfa.request.exception.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sfa.request.config.SpringContext;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.types.*;
import org.sfa.request.response.SaayamResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * ClassName: LambdaExceptionHandler
 * Package: org.sfa.request.exception.handler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/20 22:27
 * @version 1.0
 */
@Slf4j
public class LambdaExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MessageSource messageSource = SpringContext.getContext().getBean(MessageSource.class);

    public static APIGatewayProxyResponseEvent handleException(Exception e, Context context, Locale locale) {
        if (e instanceof InvalidRequestException) {
            return handleInvalidRequestException((InvalidRequestException) e, locale);
        } else if (e instanceof NotFoundException) {
            return handleNotFoundException((NotFoundException) e, locale);
        } else if (e instanceof ConflictException) {
            return handleConflictException((ConflictException) e, locale);
        } else if (e instanceof ForbiddenException) {
            return handleForbiddenException((ForbiddenException) e, locale);
        } else if (e instanceof UnauthorizedException) {
            return handleUnauthorizedException((UnauthorizedException) e, locale);
        } else if (e instanceof EnumUnspecifiedException) {
            return handleEnumUnspecifiedException((EnumUnspecifiedException) e, locale);
        } else {
            return handleGeneralException(e, locale);
        }
    }

    private static APIGatewayProxyResponseEvent handleInvalidRequestException(InvalidRequestException e, Locale locale) {
        String message = messageSource.getMessage("error.invalidRequest", new Object[]{e.getMessage()}, locale);
        log.warn("Invalid request: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, SaayamStatusCode.BAD_REQUEST, message);
    }

    private static APIGatewayProxyResponseEvent handleNotFoundException(NotFoundException e, Locale locale) {
        String message = messageSource.getMessage("error.notFound", new Object[]{e.getMessage()}, locale);
        log.warn("Request not found: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.NOT_FOUND, SaayamStatusCode.REQUEST_NOT_FOUND, message);
    }

    private static APIGatewayProxyResponseEvent handleConflictException(ConflictException e, Locale locale) {
        String message = messageSource.getMessage("error.conflict", new Object[]{e.getMessage()}, locale);
        log.warn("Conflict: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.CONFLICT, SaayamStatusCode.REQUEST_CONFLICT, message);
    }

    private static APIGatewayProxyResponseEvent handleForbiddenException(ForbiddenException e, Locale locale) {
        String message = messageSource.getMessage("error.forbidden", new Object[]{e.getMessage()}, locale);
        log.warn("Forbidden access: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.FORBIDDEN, SaayamStatusCode.FORBIDDEN, message);
    }

    private static APIGatewayProxyResponseEvent handleUnauthorizedException(UnauthorizedException e, Locale locale) {
        String message = messageSource.getMessage("error.unauthorized", new Object[]{e.getMessage()}, locale);
        log.warn("Unauthorized access: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, SaayamStatusCode.UNAUTHORIZED, message);
    }

    private static APIGatewayProxyResponseEvent handleEnumUnspecifiedException(EnumUnspecifiedException e, Locale locale) {
        String message = messageSource.getMessage("error.enumUnspecified", new Object[]{e.getMessage()}, locale);
        log.warn("Enum unspecified: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, SaayamStatusCode.ENUM_UNSPECIFIED, message);
    }

    private static APIGatewayProxyResponseEvent handleGeneralException(Exception e, Locale locale) {
        String exceptionName = e.getClass().getSimpleName();
        String message = messageSource.getMessage("error.general", new Object[]{exceptionName}, locale);
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SaayamStatusCode.INTERNAL_SERVER_ERROR, message);
    }

    private static APIGatewayProxyResponseEvent buildErrorResponse(HttpStatus status, SaayamStatusCode saayamStatusCode, String message) {
        SaayamResponse<Void> errorResponse = SaayamResponse.error(
                status.value(),
                saayamStatusCode,
                message
        );

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(status.value());
        try {
            responseEvent.setBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception ex) {
            log.error("Error serializing error response", ex);
            responseEvent.setBody("{\"message\":\"Internal server error\"}");
            responseEvent.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseEvent;
    }
}