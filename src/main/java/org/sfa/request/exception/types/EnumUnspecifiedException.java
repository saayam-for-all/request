package org.sfa.request.exception.types;

/**
 * ClassName: EnumUnspecifiedException
 * Package: org.sfa.request.exception.types
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/27 14:34
 * @version 1.0
 */
public class EnumUnspecifiedException extends RuntimeException {
    public EnumUnspecifiedException(String message) {
        super(message);
    }

    public EnumUnspecifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
