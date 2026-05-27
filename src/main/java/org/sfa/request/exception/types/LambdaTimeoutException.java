package org.sfa.request.exception.types;

/**
 * ClassName: LambdaTimeoutException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/20 0:46
 * @version 1.0
 */
public class LambdaTimeoutException extends BaseException {
    public LambdaTimeoutException(String message, Object... params) {
        super(message, params);
    }

    public LambdaTimeoutException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }
}
