package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.sfa.request.dto.*;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {RequestController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class RequestControllerTest {

    private static final String BASE = "/dev/requests/v1.0.0/requests";

    @MockBean private LocaleResolver localeResolver;
    @MockBean private RequestService requestService;
    @Autowired private RequestController requestController;

    private SaayamResponse<Request> sampleResponse() {
        return SaayamResponse.<Request>builder()
                .data(new Request())
                .message("success")
                .saayamCode("SAAYAM-1201")
                .statusCode(200)
                .success(true)
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
    }

    private String requestDtoJson() throws Exception {
        RequestDTO dto = new RequestDTO();
        dto.setRequesterId("42");
        dto.setRequestSubject("Need help");
        dto.setRequestDescription("Description");
        dto.setRequestStatus(new RequestStatusDTO());
        dto.setRequestPriority(new RequestPriorityDTO());
        dto.setRequestType(new RequestTypeDTO());
        dto.setHelpCategory(new HelpCategoryDto());
        dto.setRequestFor(new RequestForDTO());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(dto);
    }

    @Test
    void testCreateRequest() throws Exception {
        when(requestService.createRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(sampleResponse());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetRequestById() throws Exception {
        when(requestService.getRequestById(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(sampleResponse());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(get(BASE + "/{requestId}", "42").param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetRequests() throws Exception {
        SaayamResponse<PagedResponse<Request>> response = SaayamResponse.<PagedResponse<Request>>builder()
                .data(new PagedResponse<>()).success(true).statusCode(200)
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.getRequests(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(response);
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build()
                .perform(get(BASE).param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMyRequests() throws Exception {
        SaayamResponse<PagedResponse<Request>> response = SaayamResponse.<PagedResponse<Request>>builder()
                .data(new PagedResponse<>()).success(true).statusCode(200)
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.getRequests(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(response);
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build()
                .perform(get(BASE + "/my").param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateRequest() throws Exception {
        when(requestService.updateRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(sampleResponse());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(put(BASE + "/{requestId}", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteRequest() throws Exception {
        when(requestService.deleteRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(SaayamResponse.<Void>builder().success(true).statusCode(200)
                        .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC)).build());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(delete(BASE + "/{requestId}", "42").param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCancelRequest() throws Exception {
        when(requestService.cancelRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(sampleResponse());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(post(BASE + "/{requestId}/cancel", "42").param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testResumeRequest() throws Exception {
        when(requestService.resumeRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(sampleResponse());
        when(localeResolver.resolveLocale(Mockito.any(HttpServletRequest.class)))
                .thenReturn(Locale.getDefault());

        MockMvcBuilders.standaloneSetup(requestController).build()
                .perform(post(BASE + "/{requestId}/resume", "42").param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}