package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sfa.request.dto.FindNearbyVolunteersResponse;
import org.sfa.request.dto.NearestVolunteerDto;
import org.sfa.request.dto.NearestVolunteersRequest;
import org.sfa.request.dto.UpdateVolunteerLocationRequest;
import org.sfa.request.dto.UpdateVolunteerLocationResponse;
import org.sfa.request.service.api.GeoSpatialService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoSpatialServiceImpl implements GeoSpatialService {

    private static final Logger logger = LoggerFactory.getLogger(GeoSpatialServiceImpl.class);

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${geo.lambda.enabled:false}")
    private boolean geoLambdaEnabled;

    @Value("${geo.lambda.update-location-function:}")
    private String updateLocationFunction;

    @Value("${geo.lambda.find-nearby-volunteers-function:}")
    private String findNearbyVolunteersFunction;

    @Value("${geo.api.default-radius:25}")
    private Double defaultRadius;

    @Value("${geo.api.default-limit:10}")
    private Integer defaultLimit;

    @Value("${geo.api.default-unit:km}")
    private String defaultUnit;

    @Override
    public UpdateVolunteerLocationResponse updateVolunteerLocation(String userId, String address) {
        if (!geoLambdaEnabled || userId == null || userId.isBlank() || address == null || address.isBlank()) {
            logger.info("Skipping updateVolunteerLocation Lambda: enabled={}, userIdPresent={}, addressPresent={}",
                    geoLambdaEnabled,
                    userId != null && !userId.isBlank(),
                    address != null && !address.isBlank());
            return null;
        }

        if (updateLocationFunction == null || updateLocationFunction.isBlank()) {
            logger.warn("Geo updateVolunteerLocation Lambda function name is missing");
            return null;
        }

        UpdateVolunteerLocationRequest request = new UpdateVolunteerLocationRequest(userId, address);

        try {
            String payload = objectMapper.writeValueAsString(request);

            logger.info("Direct Lambda updateVolunteerLocation function={}", updateLocationFunction);
            logger.info("Direct Lambda updateVolunteerLocation payload userId={}, address={}", userId, address);

            InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                    .functionName(updateLocationFunction)
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build());

            String responsePayload = response.payload().asUtf8String();

            logger.info("Direct Lambda updateVolunteerLocation statusCode={}", response.statusCode());
            logger.info("Direct Lambda updateVolunteerLocation response={}", responsePayload);

            if (responsePayload == null || responsePayload.isBlank()) {
                return null;
            }

            return objectMapper.readValue(responsePayload, UpdateVolunteerLocationResponse.class);

        } catch (Exception ex) {
            logger.warn("Direct Lambda updateVolunteerLocation failed. function={}, userId={}, error={}",
                    updateLocationFunction, userId, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public List<NearestVolunteerDto> findNearestVolunteers(Double latitude, Double longitude) {
        if (!geoLambdaEnabled || latitude == null || longitude == null) {
            logger.info("Skipping findNearestVolunteers Lambda: enabled={}, latitude={}, longitude={}",
                    geoLambdaEnabled, latitude, longitude);
            return Collections.emptyList();
        }

        if (findNearbyVolunteersFunction == null || findNearbyVolunteersFunction.isBlank()) {
            logger.warn("Geo findNearbyVolunteers Lambda function name is missing");
            return Collections.emptyList();
        }

        NearestVolunteersRequest request =
                new NearestVolunteersRequest(latitude, longitude, defaultLimit, defaultRadius, defaultUnit);

        try {
            String payload = objectMapper.writeValueAsString(request);

            logger.info("Direct Lambda findNearbyVolunteers function={}", findNearbyVolunteersFunction);
            logger.info("Direct Lambda findNearbyVolunteers payload lat={}, lon={}, radius={}, limit={}, unit={}",
                    latitude, longitude, defaultRadius, defaultLimit, defaultUnit);

            InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                    .functionName(findNearbyVolunteersFunction)
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build());

            String responsePayload = response.payload().asUtf8String();

            logger.info("Direct Lambda findNearbyVolunteers statusCode={}", response.statusCode());
            logger.info("Direct Lambda findNearbyVolunteers response={}", responsePayload);

            if (responsePayload == null || responsePayload.isBlank()) {
                return Collections.emptyList();
            }

            FindNearbyVolunteersResponse responseBody =
                    objectMapper.readValue(responsePayload, FindNearbyVolunteersResponse.class);

            if (responseBody == null || responseBody.getBody() == null) {
                return Collections.emptyList();
            }

            return responseBody.getBody();

        } catch (Exception ex) {
            logger.warn("Direct Lambda findNearbyVolunteers failed. function={}, lat={}, lon={}, error={}",
                    findNearbyVolunteersFunction, latitude, longitude, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}