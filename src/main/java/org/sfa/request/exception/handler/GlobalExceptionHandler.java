package org.sfa.request.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.types.*;
import org.sfa.request.response.SaayamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * ClassName: GlobalExceptionHandler
 * Package: org.sfa.request.exception.handler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/26 23:35
 * @version 1.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<SaayamResponse<Void>> handleInvalidRequestException(InvalidRequestException ex, Locale locale) {
        String message = messageSource.getMessage("error.invalidRequest", new Object[]{ex.getMessage()}, locale);
        log.warn("Invalid request: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
                SaayamResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        SaayamStatusCode.BAD_REQUEST,
                        message
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SaayamResponse<Void>> handleNotFoundException(NotFoundException ex, Locale locale) {
        String message = ex.getMessage();
        log.warn("Request not found: {}", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                SaayamResponse.error(
                        HttpStatus.NOT_FOUND.value(),
                        SaayamStatusCode.REQUEST_NOT_FOUND,
                        message
                )
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<SaayamResponse<Void>> handleConflictException(ConflictException ex, Locale locale) {
        String message = messageSource.getMessage("error.conflict", new Object[]{ex.getMessage()}, locale);
        log.warn("Conflict: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                SaayamResponse.error(
                        HttpStatus.CONFLICT.value(),
                        SaayamStatusCode.REQUEST_CONFLICT,
                        message
                )
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<SaayamResponse<Void>> handleForbiddenException(ForbiddenException ex, Locale locale) {
        String message = messageSource.getMessage("error.forbidden", new Object[]{ex.getMessage()}, locale);
        log.warn("Forbidden access: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                SaayamResponse.error(
                        HttpStatus.FORBIDDEN.value(),
                        SaayamStatusCode.FORBIDDEN,
                        message
                )
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<SaayamResponse<Void>> handleUnauthorizedException(UnauthorizedException ex, Locale locale) {
        String message = messageSource.getMessage("error.unauthorized", new Object[]{ex.getMessage()}, locale);
        log.warn("Unauthorized access: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                SaayamResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        SaayamStatusCode.UNAUTHORIZED,
                        message
                )
        );
    }

    @ExceptionHandler(EnumUnspecifiedException.class)
    public ResponseEntity<SaayamResponse<Void>> handleEnumUnspecifiedException(EnumUnspecifiedException ex, Locale locale) {
        String message = ex.getMessage();
        log.warn("Enum unspecified: {}", message);
        return ResponseEntity.badRequest().body(
                SaayamResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        SaayamStatusCode.ENUM_UNSPECIFIED,
                        message
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SaayamResponse<Void>> handleGeneralException(Exception ex, Locale locale) {
        String exceptionName = ex.getClass().getSimpleName();
        String message = messageSource.getMessage("error.general", new Object[]{exceptionName}, locale);
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SaayamResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        SaayamStatusCode.INTERNAL_SERVER_ERROR,
                        message
                )
        );
    }
}