package org.sfa.request.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipient {
    private String userId;
    private NotificationChannel channel;
    private String targetAddress;
}
