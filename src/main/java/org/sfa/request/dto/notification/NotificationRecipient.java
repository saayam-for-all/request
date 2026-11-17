package org.sfa.request.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipient {
    private String userId;
    @ToString.Include
    private NotificationChannel channel;
    private String targetAddress;
}
