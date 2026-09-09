package org.sfa.request.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private NotificationEventType eventType;
    private String requestId;
    private String requesterId;
    private List<NotificationRecipient> recipients;
    private Locale locale;
    private String subject;
    private String body;
    private Map<String, String> attributes;
    private ZonedDateTime createdAt;
}
