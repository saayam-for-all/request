package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestGuestDetails;
import org.sfa.request.repository.RequestGuestDetailsRepository;
import org.sfa.request.service.api.NotificationRecipientResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationRecipientResolverImpl implements NotificationRecipientResolver {

    private final RequestGuestDetailsRepository requestGuestDetailsRepository;

    @Override
    public List<NotificationRecipient> resolveRequestRecipients(Request request) {
        List<NotificationRecipient> recipients = new ArrayList<>();

        requestGuestDetailsRepository.findById(request.getRequestId())
                .ifPresent(guestDetails -> addGuestRecipients(recipients, request, guestDetails));

        if (recipients.isEmpty()) {
            recipients.add(new NotificationRecipient(
                    request.getRequesterId(),
                    NotificationChannel.EMAIL,
                    null
            ));
        }

        return recipients;
    }

    private void addGuestRecipients(
            List<NotificationRecipient> recipients,
            Request request,
            RequestGuestDetails guestDetails
    ) {
        if (StringUtils.hasText(guestDetails.getReqEmail())) {
            recipients.add(new NotificationRecipient(
                    request.getRequesterId(),
                    NotificationChannel.EMAIL,
                    guestDetails.getReqEmail()
            ));
        }

        if (StringUtils.hasText(guestDetails.getReqPhone())) {
            recipients.add(new NotificationRecipient(
                    request.getRequesterId(),
                    NotificationChannel.SMS,
                    guestDetails.getReqPhone()
            ));
        }
    }
}
