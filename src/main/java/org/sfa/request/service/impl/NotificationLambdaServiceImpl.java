package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.NearestVolunteerDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.service.api.NotificationLambdaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationLambdaServiceImpl implements NotificationLambdaService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${notifications.lambda.function-name:}")
    private String notificationLambdaName;

    @Override
    public void notifyNearbyVolunteers(Request savedRequest, RequestDTO requestDTO, List<NearestVolunteerDto> volunteers) {
        try {
            if (notificationLambdaName == null || notificationLambdaName.isBlank()) {
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", savedRequest.getRequestId());
            payload.put("requesterId", savedRequest.getCreatorId());
            payload.put("requestType", "IN_PERSON");
            payload.put("subject", savedRequest.getRequestSubject());
            payload.put("description", savedRequest.getRequestDescription());
            payload.put("latitude", requestDTO.getLatitude());
            payload.put("longitude", requestDTO.getLongitude());
            payload.put("volunteers", volunteers);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            lambdaClient.invoke(InvokeRequest.builder()
                    .functionName(notificationLambdaName)
                    .invocationType(InvocationType.EVENT)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build());

        } catch (Exception ex) {
            // do not fail request creation if notification fails
        }
    }
}