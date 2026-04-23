package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.request.dto.RequestNoteDTO;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestNote;
import org.sfa.request.repository.RequestNoteRepository;
import org.sfa.request.repository.RequestRepository;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    RequestRepository requestRepository;
    @Mock
    RequestNoteRepository noteRepository;
    @Mock
    MessageSource messageSource;

    @InjectMocks
    NoteServiceImpl noteService;

    @Test
    void getNotes_returnsDtos() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(eq("REQ-1"), eq("SID-1")))
                .thenReturn(Optional.of(req));
        when(noteRepository.findByRequestOrderByCreatedAtDesc(req)).thenReturn(List.of(new RequestNote()));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        var resp = noteService.getNotes("SID-1", "REQ-1", Locale.ENGLISH);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData()).hasSize(1);
    }

    @Test
    void addNote_savesEntity() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(eq("REQ-1"), eq("SID-1")))
                .thenReturn(Optional.of(req));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");
        when(noteRepository.save(any(RequestNote.class))).thenAnswer(inv -> {
            RequestNote n = inv.getArgument(0);
            n.setNoteId(100L);
            return n;
        });

        var dtoIn = new RequestNoteDTO(null, "SID-U", "HELPER", "hello", null);
        var resp = noteService.addNote("SID-1", "REQ-1", dtoIn, Locale.ENGLISH);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData().getNoteId()).isEqualTo(100L);

        ArgumentCaptor<RequestNote> cap = ArgumentCaptor.forClass(RequestNote.class);
        verify(noteRepository).save(cap.capture());
        assertThat(cap.getValue().getContent()).isEqualTo("hello");
    }

    @Test
    void findRequest_notFound_throws() {
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("not found");
        assertThatThrownBy(() -> noteService.getNotes("SID-1", "REQ-1", Locale.ENGLISH))
                .isInstanceOf(NotFoundException.class);
    }
}
