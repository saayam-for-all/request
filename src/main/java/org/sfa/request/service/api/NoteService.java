package org.sfa.request.service.api;

import org.sfa.request.dto.RequestNoteDTO;
import org.sfa.request.response.SaayamResponse;

import java.util.List;
import java.util.Locale;

public interface NoteService {
    SaayamResponse<List<RequestNoteDTO>> getNotes(String requesterId, String requestId, Locale locale);
    SaayamResponse<RequestNoteDTO> addNote(String requesterId, String requestId, RequestNoteDTO noteDTO, Locale locale);
}
