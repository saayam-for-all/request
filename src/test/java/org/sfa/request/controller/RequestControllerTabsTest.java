package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.HelperVolunteerDTO;
import org.sfa.request.dto.RequestNoteDTO;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NoteService;
import org.sfa.request.service.api.RequestService;
import org.sfa.request.service.api.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RequestController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class RequestControllerTabsTest {

    @Autowired
    private RequestController requestController;

    @MockBean
    private LocaleResolver localeResolver;

    @MockBean
    private RequestService requestService; // not used here but required due to @Autowired in controller

    @MockBean
    private NoteService noteService;

    @MockBean
    private VolunteerService volunteerService;

    @Test
    void getNotes_returnsList() throws Exception {
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);
        var notes = List.of(new RequestNoteDTO(1L, "SID-USER1", "LEAD_VOLUNTEER", "content", ZonedDateTime.now()));
        when(noteService.getNotes(anyString(), anyString(), any())).thenReturn(
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "ok", notes)
        );

        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(MockMvcRequestBuilders
                        .get("/api/v1.0.0/requests/{requesterId}/{requestId}/notes", "SID-REQ", "REQ-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].authorUserId").value("SID-USER1"));
    }

    @Test
    void addNote_createsNote() throws Exception {
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);
        var saved = new RequestNoteDTO(10L, "SID-USER2", "HELPER", "hello", ZonedDateTime.now());
        when(noteService.addNote(anyString(), anyString(), any(RequestNoteDTO.class), any())).thenReturn(
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "ok", saved)
        );

        String body = new ObjectMapper().writeValueAsString(new RequestNoteDTO(null, "SID-USER2", "HELPER", "hello", null));

        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(MockMvcRequestBuilders
                        .post("/api/v1.0.0/requests/{requesterId}/{requestId}/notes", "SID-REQ", "REQ-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.noteId").value(10));
    }

    @Test
    void getHelpers_returnsList() throws Exception {
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);
        var helpers = List.of(new HelperVolunteerDTO("SID-V1", "SID-L1", ZonedDateTime.now()));
        when(volunteerService.getHelpers(anyString(), anyString(), any())).thenReturn(
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "ok", helpers)
        );

        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(MockMvcRequestBuilders
                        .get("/api/v1.0.0/requests/{requesterId}/{requestId}/helpers", "SID-REQ", "REQ-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].volunteerUserId").value("SID-V1"));
    }

    @Test
    void addHelper_createsHelper() throws Exception {
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);
        var out = new HelperVolunteerDTO("SID-V2", "SID-L1", ZonedDateTime.now());
        when(volunteerService.addHelper(anyString(), anyString(), any(HelperVolunteerDTO.class), any())).thenReturn(
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "ok", out)
        );
        String body = new ObjectMapper().writeValueAsString(new HelperVolunteerDTO("SID-V2", "SID-L1", null));

        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(MockMvcRequestBuilders
                        .post("/api/v1.0.0/requests/{requesterId}/{requestId}/helpers", "SID-REQ", "REQ-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.volunteerUserId").value("SID-V2"));
    }

    @Test
    void removeHelper_deletes() throws Exception {
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);
        when(volunteerService.removeHelper(anyString(), anyString(), anyString(), any())).thenReturn(
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "ok", null)
        );

        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(MockMvcRequestBuilders
                        .delete("/api/v1.0.0/requests/{requesterId}/{requestId}/helpers/{volunteerUserId}",
                                "SID-REQ", "REQ-1", "SID-V2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }
}
