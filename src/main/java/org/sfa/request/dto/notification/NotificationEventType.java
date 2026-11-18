package org.sfa.request.dto.notification;

public enum NotificationEventType {
    REQUEST_CREATED,
    REQUEST_UPDATED,
    REQUEST_CANCELLED,
    REQUEST_RESUMED,
    REQUEST_DELETED,
    VOLUNTEER_CHOSEN,
    /**
     * A verification code for Part 3 of the workflow (issue #95). Sent directly by SMS
     * and email only; never queued on SQS or stored as an in-app notification, so the
     * code does not persist anywhere outside the recipient's message.
     */
    OTP_ISSUED
}
