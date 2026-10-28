package org.sfa.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.*;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.*;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.repository.*;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RequestServiceImplStatusResolutionTest {
    private static final String REQUESTER_ID = "SID-1";
    private static final String REQUEST_ID = "REQ-1";

    private final RequestRepository requests = mock(RequestRepository.class);
    private final RequestStatusRepository statuses = mock(RequestStatusRepository.class);
    private final RequestPriorityRepository priorities = mock(RequestPriorityRepository.class);
    private final RequestTypeRepository types = mock(RequestTypeRepository.class);
    private final HelpCategoryRepository categories = mock(HelpCategoryRepository.class);
    private final RequestForRepository requestFors = mock(RequestForRepository.class);
    private final RequestIsLeadVolunteerRepository leadFlags = mock(RequestIsLeadVolunteerRepository.class);
    private final VolunteerAssignmentService assignments = mock(VolunteerAssignmentService.class);
    private final NotificationEventService notifications = mock(NotificationEventService.class);
    private final MessageSource messages = mock(MessageSource.class);
    private RequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RequestServiceImpl(requests, mock(RequestGuestDetailsRepository.class), statuses,
                leadFlags, priorities, types, categories, requestFors, assignments, notifications, messages);
        when(messages.getMessage(anyString(), any(), any(Locale.class))).thenReturn("message");
        when(requests.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createUsesLookupNameEvenWhenCreatedHasDatabaseCsvIdZero() {
        RequestStatus created = status(0, RequestStatusEnum.CREATED);
        when(statuses.findByStatus("CREATED")).thenReturn(Optional.of(created));
        when(priorities.findById(1)).thenReturn(Optional.of(new RequestPriority()));
        when(types.findById(2)).thenReturn(Optional.of(new RequestType()));
        when(categories.findById("CAT-1")).thenReturn(Optional.of(new HelpCategory()));
        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(2);
        when(requestFors.findById(2)).thenReturn(Optional.of(requestFor));
        when(leadFlags.findById(0)).thenReturn(Optional.of(new RequestIsLeadVolunteer()));

        RequestDTO dto = new RequestDTO();
        dto.setRequestPriority(new RequestPriorityDTO(1));
        dto.setRequestType(new RequestTypeDTO(2));
        HelpCategoryDto category = new HelpCategoryDto();
        category.setCatId("CAT-1");
        dto.setHelpCategory(category);
        dto.setRequestFor(new RequestForDTO(2));
        dto.setIsLeadVolunteer(0);

        Request createdRequest = service.createRequest(REQUESTER_ID, dto, Locale.US).getData();

        assertSame(created, createdRequest.getRequestStatus());
        verify(statuses).findByStatus("CREATED");
        verify(statuses, never()).findById(anyInt());
        verify(requests).save(any(Request.class));
    }

    @Test
    void missingCreatedRowFailsBeforePersistenceOrNotification() {
        when(priorities.findById(1)).thenReturn(Optional.of(new RequestPriority()));
        when(types.findById(2)).thenReturn(Optional.of(new RequestType()));
        when(categories.findById("CAT-1")).thenReturn(Optional.of(new HelpCategory()));
        when(requestFors.findById(2)).thenReturn(Optional.of(new RequestFor()));
        RequestDTO dto = new RequestDTO();
        dto.setRequestPriority(new RequestPriorityDTO(1));
        dto.setRequestType(new RequestTypeDTO(2));
        HelpCategoryDto category = new HelpCategoryDto();
        category.setCatId("CAT-1");
        dto.setHelpCategory(category);
        dto.setRequestFor(new RequestForDTO(2));
        dto.setIsLeadVolunteer(0);

        assertThrows(NotFoundException.class, () -> service.createRequest(REQUESTER_ID, dto, Locale.US));
        verify(requests, never()).save(any());
        verifyNoInteractions(notifications);
    }

    @Test
    void cancelAndResumeUseNamesWithDatabaseCsvIds() {
        Request request = request(status(0, RequestStatusEnum.CREATED));
        RequestStatus cancelled = status(4, RequestStatusEnum.CANCELLED);
        RequestStatus created = status(0, RequestStatusEnum.CREATED);
        when(requests.findActiveByRequestIdAndRequesterId(REQUEST_ID, REQUESTER_ID, "DELETED"))
                .thenReturn(Optional.of(request));
        when(statuses.findByStatus("CANCELLED")).thenReturn(Optional.of(cancelled));
        when(statuses.findByStatus("CREATED")).thenReturn(Optional.of(created));

        assertSame(cancelled, service.cancelRequest(REQUESTER_ID, REQUEST_ID, Locale.US).getData().getRequestStatus());
        assertThrows(InvalidRequestException.class,
                () -> service.cancelRequest(REQUESTER_ID, REQUEST_ID, Locale.US));
        assertSame(created, service.resumeRequest(REQUESTER_ID, REQUEST_ID, Locale.US).getData().getRequestStatus());
        assertThrows(InvalidRequestException.class,
                () -> service.resumeRequest(REQUESTER_ID, REQUEST_ID, Locale.US));
        verify(requests, times(2)).save(request);
    }

    @Test
    void deleteRecognizesDeletedNameRegardlessOfItsId() {
        Request request = request(status(5, RequestStatusEnum.DELETED));
        when(requests.findByRequestIdAndRequesterIdIncludingDeleted(REQUEST_ID, REQUESTER_ID))
                .thenReturn(Optional.of(request));

        assertThrows(ConflictException.class,
                () -> service.deleteRequest(REQUESTER_ID, REQUEST_ID, Locale.US));
        verify(requests, never()).save(any());
    }

    @Test
    void deleteResolvesDeployedDeletedRowByName() {
        Request request = request(status(0, RequestStatusEnum.CREATED));
        RequestStatus deleted = status(5, RequestStatusEnum.DELETED);
        when(requests.findByRequestIdAndRequesterIdIncludingDeleted(REQUEST_ID, REQUESTER_ID))
                .thenReturn(Optional.of(request));
        when(statuses.findByStatus("DELETED")).thenReturn(Optional.of(deleted));

        service.deleteRequest(REQUESTER_ID, REQUEST_ID, Locale.US);

        assertSame(deleted, request.getRequestStatus());
        verify(requests).save(request);
    }

    private static RequestStatus status(int id, RequestStatusEnum name) {
        RequestStatus status = new RequestStatus();
        status.setRequestStatusId(id);
        status.setStatus(name.name());
        return status;
    }

    private static Request request(RequestStatus status) {
        return Request.builder().requestId(REQUEST_ID).requesterId(REQUESTER_ID).requestStatus(status).build();
    }
}
