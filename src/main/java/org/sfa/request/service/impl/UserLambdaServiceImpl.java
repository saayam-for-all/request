package org.sfa.request.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.GuestDetailsDTO;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.service.api.UserLambdaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserLambdaServiceImpl implements UserLambdaService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("${user.lambda.does-user-exist-function}")
    private String doesUserExistFunction;

    @Value("${user.lambda.create-user-function}")
    private String createUserFunction;

    @Override
    public String getOrCreateUser(RequestDTO requestDTO) {

        GuestDetailsDTO guestDetails = requestDTO.getGuestDetails();

        if (guestDetails == null) {
            throw new InvalidRequestException(
                    "Guest details are required when request is for OTHER"
            );
        }

        try (LambdaClient lambdaClient = LambdaClient.builder()
                .region(Region.of(awsRegion))
                .build()) {

            JsonNode existingUser =
                    callDoesUserExist(lambdaClient, guestDetails);

            String existingUserId =
                    extractUserId(existingUser);

            if (existingUserId != null
                    && !existingUserId.isBlank()) {

                log.info(
                        "Beneficiary already exists with SID: {}",
                        existingUserId
                );

                return existingUserId;
            }

            JsonNode createdUser =
                    callCreateUser(
                            lambdaClient,
                            requestDTO,
                            guestDetails
                    );

            String createdUserId =
                    extractUserId(createdUser);

            if (createdUserId == null
                    || createdUserId.isBlank()) {

                throw new InvalidRequestException(
                        "createUserVolunteer Lambda did not return a user SID"
                );
            }

            log.info(
                    "Created beneficiary with SID: {}",
                    createdUserId
            );

            return createdUserId;

        } catch (InvalidRequestException e) {
            throw e;

        } catch (Exception e) {

            log.error(
                    "Failed while resolving OTHER user",
                    e
            );

            throw new RuntimeException(
                    "Failed to resolve beneficiary user: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private JsonNode callDoesUserExist(
            LambdaClient lambdaClient,
            GuestDetailsDTO guestDetails
    ) throws Exception {

        Map<String, Object> body = new HashMap<>();

        body.put(
                "firstName",
                guestDetails.getReqFname()
        );

        body.put(
                "lastName",
                guestDetails.getReqLname()
        );

        body.put(
                "email",
                guestDetails.getReqEmail()
        );

        body.put(
                "phone",
                guestDetails.getReqPhone()
        );

        log.info(
                "doesUserExist request body: {}",
                body
        );

        return invokeLambda(
                lambdaClient,
                doesUserExistFunction,
                body
        );
    }

    private JsonNode callCreateUser(
            LambdaClient lambdaClient,
            RequestDTO requestDTO,
            GuestDetailsDTO guestDetails
    ) throws Exception {

        Map<String, Object> body = new HashMap<>();

        body.put(
                "firstName",
                guestDetails.getReqFname()
        );

        body.put(
                "lastName",
                guestDetails.getReqLname()
        );

        body.put(
                "email",
                guestDetails.getReqEmail()
        );

        body.put(
                "phone",
                guestDetails.getReqPhone()
        );

        body.put(
                "preferredLanguage",
                guestDetails.getReqPrefLang()
        );

        body.put(
                "location",
                requestDTO.getRequestLocation()
        );

        log.info(
                "createUserVolunteer request body: {}",
                body
        );

        return invokeLambda(
                lambdaClient,
                createUserFunction,
                body
        );
    }

    private JsonNode invokeLambda(
            LambdaClient lambdaClient,
            String functionName,
            Map<String, Object> body
    ) throws Exception {

        Map<String, Object> event = new HashMap<>();

        event.put(
                "headers",
                Map.of(
                        "Content-Type", "application/json",
                        "Accept-Language", "en"
                )
        );

        event.put(
                "body",
                objectMapper.writeValueAsString(body)
        );

        InvokeRequest invokeRequest =
                InvokeRequest.builder()
                        .functionName(functionName)
                        .payload(
                                SdkBytes.fromUtf8String(
                                        objectMapper.writeValueAsString(event)
                                )
                        )
                        .build();

        InvokeResponse response =
                lambdaClient.invoke(invokeRequest);

        if (response.functionError() != null) {
            throw new InvalidRequestException(
                    "Lambda invocation failed for "
                            + functionName
            );
        }

        String responsePayload =
                response.payload().asUtf8String();

        log.info(
                "{} response: {}",
                functionName,
                responsePayload
        );

        JsonNode root =
                objectMapper.readTree(responsePayload);

        int statusCode =
                root.path("statusCode").asInt(500);

        String responseBody =
                root.path("body").asText();

        if (statusCode < 200 || statusCode >= 300) {

            String message = responseBody;

            try {
                JsonNode errorBody =
                        objectMapper.readTree(responseBody);

                message =
                        errorBody.path("message")
                                .asText(responseBody);

            } catch (Exception ignored) {
            }

            throw new InvalidRequestException(
                    functionName
                            + " failed with status "
                            + statusCode
                            + ": "
                            + message
            );
        }

        if (responseBody != null
                && !responseBody.isBlank()) {

            return objectMapper.readTree(
                    responseBody
            );
        }

        return root;
    }

    private String extractUserId(JsonNode response) {

        if (response == null) {
            return null;
        }

        if (response.hasNonNull("userId")) {
            return response.get("userId").asText();
        }

        if (response.hasNonNull("sid")) {
            return response.get("sid").asText();
        }

        JsonNode data =
                response.get("data");

        if (data != null) {

            if (data.hasNonNull("userId")) {
                return data.get("userId").asText();
            }

            if (data.hasNonNull("sid")) {
                return data.get("sid").asText();
            }
        }

        return null;
    }
}