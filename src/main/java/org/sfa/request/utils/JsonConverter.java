package org.sfa.request.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.sfa.request.dto.RequestSqsDTO;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.model.entity.Request;

/**
 * ClassName: JsonConverter
 * Package: org.sfa.request.utils
 * Description:
 *
 * @author Fan Peng
 * Create 2024/8/15 2:35
 * @version 1.0
 */
public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String convertRequestToJson(Request request) {
        RequestSqsDTO dto = new RequestSqsDTO();
        dto.setRequestId(request.getRequestId());
        dto.setRequesterId(request.getRequesterId());
        dto.setRequestStatus(request.getRequestStatus().getStatus().name());
        dto.setRequestPriority(request.getRequestPriority().getPriority().name());
        dto.setRequestType(request.getRequestType().getType().name());
        dto.setHelpCategory(request.getHelpCategory().getCatId());
        dto.setRequestFor(request.getRequestFor().getFor().name());
        dto.setCity(request.getCity());
        dto.setZipCode(request.getZipCode());
        dto.setRequestDescription(request.getRequestDescription());
        dto.setAudioRequestDescription(request.getAudioRequestDescription());
        dto.setSubmittedAt(request.getSubmittedAt() != null ? request.getSubmittedAt().toString() : null);
        dto.setServicedAt(request.getServicedAt() != null ? request.getServicedAt().toString() : null);
        dto.setLastUpdatedAt(request.getLastUpdatedAt() != null ? request.getLastUpdatedAt().toString() : null);

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Error converting request to JSON: " + e.getMessage(), e);
        }
    }
}