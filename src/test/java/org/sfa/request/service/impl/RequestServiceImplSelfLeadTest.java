package org.sfa.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestForDTO;
import org.sfa.request.dto.RequestPriorityDTO;
import org.sfa.request.dto.RequestTypeDTO;
import org.sfa.request.dto.VolunteerAssignmentDTO;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.HelpCategoryRepository;
import org.sfa.request.repository.RequestForRepository;
import org.sfa.request.repository.RequestGuestDetailsRepository;
import org.sfa.request.repository.RequestIsLeadVolunteerRepository;
import org.sfa.request.repository.RequestPriorityRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.RequestTypeRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Issue #94: when the creator answers "Yes" to "Lead Volunteer?", creating the request
 * makes them the LEAD volunteer and skips volunteer matching.
 */
class RequestServiceImplSelfLeadTest {

    private static final String REQUESTER_ID = "SID-1";
    private static final int LEAD_YES = 1;
    private static final int LEAD_NO = 0;

    private final RequestRepository requestRepository = mock(RequestRepository.class);
    private final RequestIsLeadVolunteerRepository leadRepository = mock(RequestIsLeadVolunteerRepository.class);
    private final RequestTypeRepository requestTypeRepository = mock(RequestTypeRepository.class);
    private final RequestStatusRepository requestStatusRepository = mock(RequestStatusRepository.class);
    private final RequestPriorityRepository requestPriorityRepository = mock(RequestPriorityRepository.class);
    private final HelpCategoryRepository helpCategoryRepository = mock(HelpCategoryRepository.class);
    private final RequestForRepository requestForRepository = mock(RequestForRepository.class);
    private final VolunteerAssignmentService volunteerAssignmentService = mock(VolunteerAssignmentService.class);
    private final NotificationEventService notificationEventService = mock(NotificationEventService.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private RequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RequestServiceImpl(
                requestRepository,
                mock(RequestGuestDetailsRepository.class),
                requestStatusRepository,
                leadRepository,
                requestPriorityRepository,
                requestTypeRepository,
                helpCategoryRepository,
                requestForRepository,
                volunteerAssignmentService,
                notificationEventService,
                messageSource
        );

        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("message");
        when(requestPriorityRepository.findById(anyInt())).thenReturn(Optional.of(new RequestPriority()));
        when(helpCategoryRepository.findById(anyString())).thenReturn(Optional.of(new HelpCategory()));
        RequestFor self = new RequestFor();
        self.setRequestForId(1);
        self.setRequestFor("SELF");
        when(requestForRepository.findById(anyInt())).thenReturn(Optional.of(self));
        when(requestStatusRepository.findById(anyInt())).thenReturn(Optional.of(new RequestStatus()));
        when(leadRepository.findById(LEAD_YES)).thenReturn(Optional.of(leadRow(LEAD_YES, "YES")));
        when(leadRepository.findById(LEAD_NO)).thenReturn(Optional.of(leadRow(LEAD_NO, "NO")));
        when(requestTypeRepository.findById(anyInt())).thenAnswer(inv -> Optional.of(inPersonType(inv.getArgument(0))));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request saved = inv.getArgument(0);
            saved.setRequestId("REQ-NEW");
            return saved;
        });
    }

    @Test
    void selfLeadRequestAssignsTheCreatorAsLead() {
        SaayamResponse<Request> response = service.createRequest(REQUESTER_ID, requestDTO(LEAD_YES, false), Locale.US);

        assertTrue(response.isSuccess());
        ArgumentCaptor<VolunteerAssignmentDTO> assignment = ArgumentCaptor.forClass(VolunteerAssignmentDTO.class);
        verify(volunteerAssignmentService).assignVolunteer(
                eq(REQUESTER_ID), eq("REQ-NEW"), assignment.capture(), eq(Locale.US));
        assertEquals(REQUESTER_ID, assignment.getValue().getVolunteerId());
        assertEquals(VolunteerAssignmentTypeEnum.LEAD, assignment.getValue().getVolunteerType());
    }

    /** No volunteers are notified: the creator is already the lead. */
    @Test
    void selfLeadRequestSkipsVolunteerMatching() {
        service.createRequest(REQUESTER_ID, requestDTO(LEAD_YES, true), Locale.US);

        verify(notificationEventService, never()).enqueueRequestEvent(any(), any(), any());
    }

    @Test
    void requestWithoutSelfLeadGoesToMatchingAsBefore() {
        service.createRequest(REQUESTER_ID, requestDTO(LEAD_NO, true), Locale.US);

        verify(volunteerAssignmentService, never()).assignVolunteer(any(), any(), any(), any());
        verify(notificationEventService).enqueueRequestEvent(any(), any(), eq(Locale.US));
    }

    private static RequestDTO requestDTO(int isLeadVolunteer, boolean isCalamity) {
        RequestDTO dto = new RequestDTO();
        dto.setRequesterId(REQUESTER_ID);
        dto.setRequestSubject("Flooded basement");
        dto.setRequestDescription("Need help moving things upstairs");
        dto.setRequestPriority(new RequestPriorityDTO(1));
        HelpCategoryDto category = new HelpCategoryDto();
        category.setCatId("CAT-1");
        dto.setHelpCategory(category);
        dto.setRequestFor(new RequestForDTO(1));
        dto.setRequestType(new RequestTypeDTO(RequestTypeEnum.IN_PERSON.getId()));
        dto.setIsLeadVolunteer(isLeadVolunteer);
        dto.setIsCalamity(isCalamity);
        return dto;
    }

    private static RequestIsLeadVolunteer leadRow(int id, String name) {
        RequestIsLeadVolunteer row = new RequestIsLeadVolunteer();
        row.setReqIsleadId(id);
        row.setReqIslead(name);
        return row;
    }

    private static RequestType inPersonType(Integer id) {
        RequestType type = new RequestType();
        type.setRequestTypeId(id);
        type.setType("IN_PERSON");
        return type;
    }
}
