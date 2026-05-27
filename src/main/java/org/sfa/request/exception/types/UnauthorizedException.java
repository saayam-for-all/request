package org.sfa.request.exception.types;

/**
 * ClassName: UnauthorizedException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/26 23:34
 * @version 1.0
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
