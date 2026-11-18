package org.sfa.request.requesthandler;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.OtpContextDTO;
import org.sfa.request.service.api.OtpContextService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoadOtpContextHandlerTest {

    private final OtpContextService otpContextService = mock(OtpContextService.class);
    private final LoadOtpContextHandler handler = new LoadOtpContextHandler(otpContextService);

    @Test
    void passesRequestIdToServiceAndReturnsItsResult() {
        OtpContextDTO expected = OtpContextDTO.builder()
                .requestId("REQ-1")
                .eligible(true)
                .requesterId("SID-REQUESTER")
                .leadVolunteerId("SID-LEAD")
                .build();
        when(otpContextService.loadContext("REQ-1")).thenReturn(expected);

        OtpContextDTO result = handler.handleRequest(Map.of("requestId", "REQ-1"), null);

        assertSame(expected, result);
    }

    @Test
    void ignoresExtraInputFields() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestId", "REQ-1");
        input.put("somethingElse", 42);
        when(otpContextService.loadContext("REQ-1")).thenReturn(new OtpContextDTO());

        handler.handleRequest(input, null);

        verify(otpContextService).loadContext("REQ-1");
    }

    @Test
    void missingRequestIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(Map.of(), null));
        verifyNoInteractions(otpContextService);
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(null, null));
        verifyNoInteractions(otpContextService);
    }
}
