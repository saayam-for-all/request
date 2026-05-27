package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.request.dto.HelperVolunteerDTO;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestVolunteer;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestVolunteerRepository;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceImplTest {

    @Mock
    RequestRepository requestRepository;
    @Mock
    RequestVolunteerRepository volunteerRepository;
    @Mock
    MessageSource messageSource;

    @InjectMocks
    VolunteerServiceImpl volunteerService;

    @Test
    void getHelpers_returnsList() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted("REQ-1", "SID-1"))
                .thenReturn(Optional.of(req));
        when(volunteerRepository.findByRequest(req)).thenReturn(List.of(new RequestVolunteer()));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        var resp = volunteerService.getHelpers("SID-1", "REQ-1", Locale.ENGLISH);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData()).hasSize(1);
    }

    @Test
    void addHelper_whenExists_throwsConflict() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted("REQ-1", "SID-1"))
                .thenReturn(Optional.of(req));
        when(volunteerRepository.findByRequestAndVolunteerUserId(req, "SID-V"))
                .thenReturn(Optional.of(new RequestVolunteer()));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("exists");

        assertThatThrownBy(() -> volunteerService.addHelper("SID-1", "REQ-1",
                new HelperVolunteerDTO("SID-V", "SID-L", null), Locale.ENGLISH))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void addHelper_saves() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted("REQ-1", "SID-1"))
                .thenReturn(Optional.of(req));
        when(volunteerRepository.findByRequestAndVolunteerUserId(req, "SID-V")).thenReturn(Optional.empty());
        when(volunteerRepository.save(any(RequestVolunteer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        var resp = volunteerService.addHelper("SID-1", "REQ-1",
                new HelperVolunteerDTO("SID-V", "SID-L", null), Locale.ENGLISH);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData().getVolunteerUserId()).isEqualTo("SID-V");
    }

    @Test
    void removeHelper_notFound_throws() {
        Request req = new Request();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted("REQ-1", "SID-1"))
                .thenReturn(Optional.of(req));
        when(volunteerRepository.findByRequestAndVolunteerUserId(req, "SID-V")).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("not found");

        assertThatThrownBy(() -> volunteerService.removeHelper("SID-1", "REQ-1", "SID-V", Locale.ENGLISH))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeHelper_deletes() {
        Request req = new Request();
        RequestVolunteer rv = new RequestVolunteer();
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted("REQ-1", "SID-1"))
                .thenReturn(Optional.of(req));
        when(volunteerRepository.findByRequestAndVolunteerUserId(req, "SID-V"))
                .thenReturn(Optional.of(rv));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        var resp = volunteerService.removeHelper("SID-1", "REQ-1", "SID-V", Locale.ENGLISH);
        assertThat(resp.isSuccess()).isTrue();
        verify(volunteerRepository).delete(rv);
    }
}
