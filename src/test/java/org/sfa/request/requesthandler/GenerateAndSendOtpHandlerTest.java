package org.sfa.request.requesthandler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sfa.request.dto.OtpIssueRequestDTO;
import org.sfa.request.dto.OtpIssueResultDTO;
import org.sfa.request.service.api.OtpIssueService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenerateAndSendOtpHandlerTest {

    private final OtpIssueService otpIssueService = mock(OtpIssueService.class);
    private final GenerateAndSendOtpHandler handler = new GenerateAndSendOtpHandler(otpIssueService);

    @Test
    void mapsStepFunctionsInputToTheService() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestId", "REQ-1");
        input.put("requesterId", "SID-REQUESTER");
        input.put("leadVolunteerId", "SID-LEAD");
        input.put("taskToken", "token-1");
        input.put("eligible", true); // extra fields from S0's output are ignored
        OtpIssueResultDTO expected = OtpIssueResultDTO.builder().otpVerificationId("OTP-1").build();
        when(otpIssueService.issueAndSend(any())).thenReturn(expected);

        OtpIssueResultDTO result = handler.handleRequest(input, null);

        assertSame(expected, result);
        ArgumentCaptor<OtpIssueRequestDTO> captor = ArgumentCaptor.forClass(OtpIssueRequestDTO.class);
        verify(otpIssueService).issueAndSend(captor.capture());
        assertEquals("REQ-1", captor.getValue().getRequestId());
        assertEquals("SID-REQUESTER", captor.getValue().getRequesterId());
        assertEquals("SID-LEAD", captor.getValue().getLeadVolunteerId());
        assertEquals("token-1", captor.getValue().getTaskToken());
    }

    @Test
    void missingFieldsArePassedAsNullForTheServiceToReject() {
        when(otpIssueService.issueAndSend(any())).thenThrow(new IllegalArgumentException("taskToken is required"));

        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of("requestId", "REQ-1"), null));
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(null, null));
        verifyNoInteractions(otpIssueService);
    }

    @Test
    void taskTokenIsNotInToString() {
        OtpIssueRequestDTO dto = OtpIssueRequestDTO.builder().requestId("REQ-1").taskToken("secret-token").build();

        assertFalse(dto.toString().contains("secret-token"));
    }
}
