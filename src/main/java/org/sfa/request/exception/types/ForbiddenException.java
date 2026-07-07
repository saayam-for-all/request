package org.sfa.request.exception.types;

/**
 * ClassName: ForbiddenException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/26 23:34
 * @version 1.0
 */
public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
