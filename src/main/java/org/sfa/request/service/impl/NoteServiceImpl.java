package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.RequestNoteDTO;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestNote;
import org.sfa.request.repository.RequestNoteRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NoteService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final RequestRepository requestRepository;
    private final RequestNoteRepository noteRepository;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<List<RequestNoteDTO>> getNotes(String requesterId, String requestId, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        List<RequestNoteDTO> notes = noteRepository.findByRequestOrderByCreatedAtDesc(request)
                .stream()
                .map(n -> new RequestNoteDTO(n.getNoteId(), n.getAuthorUserId(), n.getPersona(), n.getContent(), n.getCreatedAt()))
                .collect(Collectors.toList());
        String message = messageSource.getMessage("success.notesRetrieved", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, notes);
    }

    @Override
    @Transactional
    public SaayamResponse<RequestNoteDTO> addNote(String requesterId, String requestId, RequestNoteDTO noteDTO, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        RequestNote toSave = RequestNote.builder()
                .request(request)
                .authorUserId(noteDTO.getAuthorUserId())
                .persona(noteDTO.getPersona())
                .content(noteDTO.getContent())
                .build();
        RequestNote saved = noteRepository.save(toSave);
        RequestNoteDTO dto = new RequestNoteDTO(saved.getNoteId(), saved.getAuthorUserId(), saved.getPersona(), saved.getContent(), saved.getCreatedAt());
        String message = messageSource.getMessage("success.noteAdded", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, dto);
    }

    private Request findRequest(String requesterId, String requestId, Locale locale) {
        return requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(requestId, requesterId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.requestNotFound", new Object[]{requestId, requesterId}, locale)
                ));
    }
}
