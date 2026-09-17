package org.sfa.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestForDTO;
import org.sfa.request.dto.RequestPriorityDTO;
import org.sfa.request.dto.RequestStatusDTO;
import org.sfa.request.dto.RequestTypeDTO;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.repository.HelpCategoryRepository;
import org.sfa.request.repository.RequestForRepository;
import org.sfa.request.repository.RequestGuestDetailsRepository;
import org.sfa.request.repository.RequestIsLeadVolunteerRepository;
import org.sfa.request.repository.RequestPriorityRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.RequestTypeRepository;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplStatusTest {

    private static final String REQUESTER_ID = "requester-1";
    private static final String REQUEST_ID = "REQ-1";
    private static final Locale LOCALE = Locale.ENGLISH;

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestGuestDetailsRepository requestGuestDetailsRepository;
    @Mock
    private RequestStatusRepository requestStatusRepository;
    @Mock
    private RequestIsLeadVolunteerRepository requestIsLeadVolunteerRepository;
    @Mock
    private RequestPriorityRepository requestPriorityRepository;
    @Mock
    private RequestTypeRepository requestTypeRepository;
    @Mock
    private HelpCategoryRepository helpCategoryRepository;
    @Mock
    private RequestForRepository requestForRepository;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RequestServiceImpl requestService;

    @BeforeEach
    void setUpMessages() {
        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("message");
    }

    @Test
    void createUsesCreatedStatus() {
        RequestDTO requestDTO = validRequestDTO();
        RequestStatus created = status(RequestStatusEnum.CREATED);

        when(requestPriorityRepository.findById(1)).thenReturn(Optional.of(new RequestPriority()));
        when(requestTypeRepository.findById(1)).thenReturn(Optional.of(new RequestType()));
        when(helpCategoryRepository.findById("general")).thenReturn(Optional.of(new HelpCategory()));
        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(0);
        when(requestForRepository.findById(0)).thenReturn(Optional.of(requestFor));
        when(requestStatusRepository.findById(RequestStatusEnum.CREATED.getId())).thenReturn(Optional.of(created));
        when(requestIsLeadVolunteerRepository.findById(0)).thenReturn(Optional.of(new RequestIsLeadVolunteer()));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request request = invocation.getArgument(0);
            request.setRequestId(REQUEST_ID);
            return request;
        });

        requestService.createRequest(REQUESTER_ID, requestDTO, LOCALE);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(RequestStatusEnum.CREATED.getId(), captor.getValue().getRequestStatus().getRequestStatusId());
    }

    @Test
    void cancelUsesCancelledStatus() {
        Request request = requestWithStatus(RequestStatusEnum.CREATED);
        RequestStatus cancelled = status(RequestStatusEnum.CANCELLED);
        when(requestRepository.findActiveByRequestIdAndRequesterId(
                REQUEST_ID, REQUESTER_ID, RequestStatusEnum.DELETED.getId()))
                .thenReturn(Optional.of(request));
        when(requestStatusRepository.findById(RequestStatusEnum.CANCELLED.getId()))
                .thenReturn(Optional.of(cancelled));
        when(requestRepository.save(request)).thenReturn(request);

        requestService.cancelRequest(REQUESTER_ID, REQUEST_ID, LOCALE);

        assertEquals(RequestStatusEnum.CANCELLED.getId(), request.getRequestStatus().getRequestStatusId());
    }

    @Test
    void deleteUsesDeletedStatus() {
        Request request = requestWithStatus(RequestStatusEnum.IN_PROGRESS);
        RequestStatus deleted = status(RequestStatusEnum.DELETED);
        when(requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(REQUEST_ID, REQUESTER_ID))
                .thenReturn(Optional.of(request));
        when(requestStatusRepository.findById(RequestStatusEnum.DELETED.getId()))
                .thenReturn(Optional.of(deleted));
        when(requestRepository.save(request)).thenReturn(request);

        requestService.deleteRequest(REQUESTER_ID, REQUEST_ID, LOCALE);

        assertEquals(RequestStatusEnum.DELETED.getId(), request.getRequestStatus().getRequestStatusId());
    }

    @Test
    void resumeCancelledRequestUsesCreatedStatus() {
        Request request = requestWithStatus(RequestStatusEnum.CANCELLED);
        RequestStatus created = status(RequestStatusEnum.CREATED);
        when(requestRepository.findActiveByRequestIdAndRequesterId(
                REQUEST_ID, REQUESTER_ID, RequestStatusEnum.DELETED.getId()))
                .thenReturn(Optional.of(request));
        when(requestStatusRepository.findById(RequestStatusEnum.CREATED.getId()))
                .thenReturn(Optional.of(created));
        when(requestRepository.save(request)).thenReturn(request);

        requestService.resumeRequest(REQUESTER_ID, REQUEST_ID, LOCALE);

        assertEquals(RequestStatusEnum.CREATED.getId(), request.getRequestStatus().getRequestStatusId());
    }

    @Test
    void missingCanonicalStatusProducesUsefulNotFoundError() {
        RequestDTO requestDTO = validRequestDTO();
        when(requestPriorityRepository.findById(1)).thenReturn(Optional.of(new RequestPriority()));
        when(requestTypeRepository.findById(1)).thenReturn(Optional.of(new RequestType()));
        when(helpCategoryRepository.findById("general")).thenReturn(Optional.of(new HelpCategory()));
        when(requestForRepository.findById(0)).thenReturn(Optional.of(new RequestFor()));
        when(requestStatusRepository.findById(RequestStatusEnum.CREATED.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> requestService.createRequest(REQUESTER_ID, requestDTO, LOCALE)
        );

        assertEquals("message", exception.getMessage());
    }

    private static RequestDTO validRequestDTO() {
        HelpCategoryDto helpCategory = new HelpCategoryDto();
        helpCategory.setCatId("general");

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setRequesterId(REQUESTER_ID);
        requestDTO.setRequestSubject("subject");
        requestDTO.setRequestDescription("description");
        requestDTO.setRequestStatus(new RequestStatusDTO(RequestStatusEnum.UNSPECIFIED.getId()));
        requestDTO.setRequestPriority(new RequestPriorityDTO(1));
        requestDTO.setRequestType(new RequestTypeDTO(1));
        requestDTO.setHelpCategory(helpCategory);
        requestDTO.setRequestFor(new RequestForDTO(0));
        requestDTO.setIsLeadVolunteer(0);
        return requestDTO;
    }

    private static Request requestWithStatus(RequestStatusEnum requestStatus) {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER_ID);
        request.setRequestStatus(status(requestStatus));
        return request;
    }

    private static RequestStatus status(RequestStatusEnum requestStatus) {
        RequestStatus status = new RequestStatus();
        status.setRequestStatusId(requestStatus.getId());
        status.setStatus(requestStatus.name());
        return status;
    }
}
