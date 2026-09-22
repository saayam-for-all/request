package org.sfa.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestForDTO;
import org.sfa.request.dto.RequestPriorityDTO;
import org.sfa.request.dto.RequestSummaryDTO;
import org.sfa.request.dto.RequestTypeDTO;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.repository.HelpCategoryRepository;
import org.sfa.request.repository.RequestForRepository;
import org.sfa.request.repository.RequestGuestDetailsRepository;
import org.sfa.request.repository.RequestIsLeadVolunteerRepository;
import org.sfa.request.repository.RequestPriorityRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.RequestTypeRepository;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the two issue #14 behaviours that live in RequestServiceImpl:
 * request type defaulting to REMOTE, and the dashboard list projecting to
 * RequestSummaryDTO (which carries no request type).
 */
class RequestServiceImplIssue14Test {

    private static final String REQUESTER_ID = "SID-1";
    private static final String REQUEST_ID = "REQ-1";

    private final RequestRepository requestRepository = mock(RequestRepository.class);
    private final RequestGuestDetailsRepository requestGuestDetailsRepository = mock(RequestGuestDetailsRepository.class);
    private final RequestStatusRepository requestStatusRepository = mock(RequestStatusRepository.class);
    private final RequestIsLeadVolunteerRepository requestIsLeadVolunteerRepository = mock(RequestIsLeadVolunteerRepository.class);
    private final RequestPriorityRepository requestPriorityRepository = mock(RequestPriorityRepository.class);
    private final RequestTypeRepository requestTypeRepository = mock(RequestTypeRepository.class);
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
                requestGuestDetailsRepository,
                requestStatusRepository,
                requestIsLeadVolunteerRepository,
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
        when(requestForRepository.findById(anyInt())).thenReturn(Optional.of(requestFor(2)));
        when(requestStatusRepository.findById(anyInt())).thenReturn(Optional.of(new RequestStatus()));
        when(requestIsLeadVolunteerRepository.findById(anyInt())).thenReturn(Optional.of(new RequestIsLeadVolunteer()));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** Issue #14 (1): "Change Request to Remote as default - In the Create Request Page". */
    @Test
    void createRequestWithoutRequestTypeDefaultsToRemote() {
        when(requestTypeRepository.findById(RequestTypeEnum.REMOTE.getId()))
                .thenReturn(Optional.of(requestType(RequestTypeEnum.REMOTE.getId())));

        RequestDTO dto = baseRequestDTO();
        dto.setRequestType(null);

        SaayamResponse<Request> response = service.createRequest(REQUESTER_ID, dto, Locale.US);

        assertEquals(RequestTypeEnum.REMOTE.getId(), response.getData().getRequestType().getRequestTypeId());
    }

    /** An explicitly supplied request type still wins over the REMOTE default. */
    @Test
    void createRequestHonoursAnExplicitRequestType() {
        when(requestTypeRepository.findById(RequestTypeEnum.IN_PERSON.getId()))
                .thenReturn(Optional.of(requestType(RequestTypeEnum.IN_PERSON.getId())));

        RequestDTO dto = baseRequestDTO();
        dto.setRequestType(new RequestTypeDTO(RequestTypeEnum.IN_PERSON.getId()));

        SaayamResponse<Request> response = service.createRequest(REQUESTER_ID, dto, Locale.US);

        assertEquals(RequestTypeEnum.IN_PERSON.getId(), response.getData().getRequestType().getRequestTypeId());
    }

    /**
     * Issue #14 (3): "Remove 'Type' column in Dashboard Page". The list endpoint
     * returns RequestSummaryDTO, which has no request type field at all, so the
     * dashboard cannot render a Type column from this payload.
     */
    @Test
    void getRequestsReturnsSummariesWithNoRequestType() {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER_ID);
        request.setRequestType(requestType(RequestTypeEnum.IN_PERSON.getId()));
        request.setRequestSubject("Need a ride");

        Pageable pageable = PageRequest.of(0, 10);
        when(requestRepository.findAllActiveByRequesterId(
                eq(REQUESTER_ID), eq(RequestStatusEnum.DELETED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request)));

        SaayamResponse<PagedResponse<RequestSummaryDTO>> response =
                service.getRequests(REQUESTER_ID, pageable, Locale.US);

        List<RequestSummaryDTO> content = response.getData().getContent();
        assertEquals(1, content.size());
        assertEquals(REQUEST_ID, content.get(0).getRequestId());
        assertEquals("Need a ride", content.get(0).getRequestSubject());

        assertFalse(
                hasField(RequestSummaryDTO.class, "requestType"),
                "RequestSummaryDTO must not expose a request type - the dashboard has no Type column"
        );
    }

    /**
     * Issue #14 (2): the list payload carries the lead and helping volunteers so
     * the dashboard and details views can label them separately.
     */
    @Test
    void getRequestsCarriesLeadAndHelpingVolunteers() {
        Request request = new Request();
        request.setRequestId(REQUEST_ID);
        request.setRequesterId(REQUESTER_ID);

        when(requestRepository.findAllActiveByRequesterId(
                eq(REQUESTER_ID), eq(RequestStatusEnum.DELETED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request)));

        // Stand in for VolunteerAssignmentServiceImpl reading volunteers_assigned.
        org.mockito.Mockito.doAnswer(inv -> {
            Request r = inv.getArgument(0);
            r.setLeadVolunteerUserId("VOL-LEAD");
            r.setHelpingVolunteerUserIds(List.of("VOL-HELP-1", "VOL-HELP-2"));
            return null;
        }).when(volunteerAssignmentService).populateAssignments(any(Request.class));

        SaayamResponse<PagedResponse<RequestSummaryDTO>> response =
                service.getRequests(REQUESTER_ID, PageRequest.of(0, 10), Locale.US);

        RequestSummaryDTO summary = response.getData().getContent().get(0);
        assertEquals("VOL-LEAD", summary.getLeadVolunteerUserId());
        assertEquals(List.of("VOL-HELP-1", "VOL-HELP-2"), summary.getHelpingVolunteerUserIds());
    }

    /** A request with nothing assigned reports no lead and an empty helping list. */
    @Test
    void requestWithNoAssignmentsHasNullLeadAndEmptyHelpingList() {
        Request request = Request.builder().requestId(REQUEST_ID).build();

        assertNull(request.getLeadVolunteerUserId());
        assertEquals(List.of(), request.getHelpingVolunteerUserIds());
    }

    private static boolean hasField(Class<?> type, String name) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private RequestDTO baseRequestDTO() {
        RequestDTO dto = new RequestDTO();
        dto.setRequesterId(REQUESTER_ID);
        dto.setRequestSubject("Need a ride");
        dto.setRequestDescription("Need a ride to the clinic");
        dto.setRequestPriority(new RequestPriorityDTO(1));
        dto.setHelpCategory(helpCategoryDto());
        dto.setRequestFor(new RequestForDTO(2));
        dto.setIsLeadVolunteer(1);
        return dto;
    }

    private HelpCategoryDto helpCategoryDto() {
        HelpCategoryDto dto = new HelpCategoryDto();
        dto.setCatId("CAT-1");
        return dto;
    }

    private RequestType requestType(int id) {
        RequestType type = new RequestType();
        type.setRequestTypeId(id);
        return type;
    }

    private RequestFor requestFor(int id) {
        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(id);
        return requestFor;
    }
}
