package org.sfa.request.service.impl;

import org.sfa.request.dto.CreateSaayamRequestDTO;
import org.sfa.request.service.MLService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MLServiceImpl implements MLService {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.fraud-bucket}")
    private String fraudBucketName;

    public MLServiceImpl(S3Client s3Client, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isFraudulentRequest(CreateSaayamRequestDTO request) {
        // TODO: Implement actual ML-based fraud detection logic
        // For now, returning false as a placeholder
        return false;
    }

    @Override
    public String translateDescription(String description, String targetLanguage) {
        // TODO: Implement actual translation logic
        // For now, returning the original description as a placeholder
        return description;
    }

    private void logFraudulentRequest(CreateSaayamRequestDTO request) {
        try {
            String key = String.format("fraud-requests/%s/%s.json", 
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(fraudBucketName)
                .key(key)
                .build();

            String jsonContent = objectMapper.writeValueAsString(request);
            s3Client.putObject(putObjectRequest, RequestBody.fromString(jsonContent));
        } catch (Exception e) {
            // Log the error but don't throw it to prevent blocking the main flow
            // TODO: Add proper error logging
        }
    }
} 