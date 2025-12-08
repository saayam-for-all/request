package org.sfa.request.exception.types;

/**
 * ClassName: InvalidRequestException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/18 2:37
 * @version 1.0
 */
public class InvalidRequestException extends BaseException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
