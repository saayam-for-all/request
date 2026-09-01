package org.sfa.request.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.sfa.request.constant.SaayamStatusCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/**
 * ClassName: SaayamResponse
 * Package: org.sfa.request.response
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/12 1:07
 * @version 1.0
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaayamResponse<T> {
    private boolean success;
    private int statusCode;
    private String saayamCode;
    private String message;
    private T data;
    private ZonedDateTime timestamp;

    public static <T> SaayamResponse<T> success(SaayamStatusCode statusCode, String message, T data) {
        return SaayamResponse.<T>builder()
                .success(true)
                .statusCode(statusCode == SaayamStatusCode.REQUEST_CREATED ? HttpStatus.CREATED.value() : HttpStatus.OK.value())
                .saayamCode(statusCode.getCode())
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static <T> SaayamResponse<T> error(int httpStatusCode, SaayamStatusCode statusCode, String message) {
        return SaayamResponse.<T>builder()
                .success(false)
                .statusCode(httpStatusCode)
                .saayamCode(statusCode.getCode())
                .message(message)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}