package org.sfa.request.exception.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ClassName: BaseException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/18 2:37
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {
    private final Object[] params;

    public BaseException(String message, Object... params) {
        super(message);
        this.params = params;
    }

    public BaseException(String message, Throwable cause, Object... params) {
        super(message, cause);
        this.params = params;
    }
}