package org.sfa.request.dto.notification;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    /**
     * In-app notification. Persisted to the shared {@code notifications} table and
     * served to clients by the Volunteer microservice's notification read APIs
     * ({@code GET /0.0.1/notifications/{userId}}).
     */
    IN_APP
}
