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
 * Converts Request entity to JSON (via RequestSqsDTO) for pushing to SQS.
 *
 * Updated to align with new schema (HelpCategory, requestLocation, requestSubject, isLeadVolunteer).
 *
 * @author Shariq
 * @version 2.0
 */
public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String convertRequestToJson(Request request) {
        RequestSqsDTO dto = new RequestSqsDTO();
        dto.setRequestId(request.getRequestId());
        dto.setCreatorId(request.getCreatorId());
        dto.setBeneficiaryId(request.getBeneficiaryId());
        dto.setRequestStatus(request.getRequestStatus().getStatus());
        dto.setRequestPriority(request.getRequestPriority().getPriority());
        dto.setRequestType(request.getRequestType().getType());
        dto.setHelpCategory(request.getHelpCategory().getCatName());
        dto.setRequestFor(request.getRequestFor().getRequestFor());
        dto.setRequestLocation(request.getRequestLocation());
        dto.setRequestSubject(request.getRequestSubject());
        dto.setRequestDescription(request.getRequestDescription());
        dto.setAudioRequestDescription(request.getAudioRequestDescription());
        dto.setSubmittedAt(request.getSubmittedAt() != null ? request.getSubmittedAt().toString() : null);
        dto.setIsLeadVolunteer(String.valueOf(request.getIsLeadVolunteer()));
        dto.setServicedAt(request.getServicedAt() != null ? request.getServicedAt().toString() : null);
        dto.setLastUpdatedAt(request.getLastUpdatedAt() != null ? request.getLastUpdatedAt().toString() : null);

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Error converting request to JSON: " + e.getMessage(), e);
        }
    }

}
