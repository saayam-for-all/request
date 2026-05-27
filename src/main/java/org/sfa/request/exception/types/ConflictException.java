package org.sfa.request.exception.types;

/**
 * ClassName: ConflictException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/26 23:34
 * @version 1.0
 */
public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
