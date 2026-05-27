//package org.sfa.request.controller;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.anyInt;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.util.HashSet;
//import java.util.Locale;
//import java.util.Optional;
//
//import org.junit.jupiter.api.Disabled;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.sfa.request.dto.RequestCategoryDTO;
//import org.sfa.request.dto.RequestDTO;
//import org.sfa.request.dto.RequestForDTO;
//import org.sfa.request.dto.RequestPriorityDTO;
//import org.sfa.request.dto.RequestStatusDTO;
//import org.sfa.request.dto.RequestTypeDTO;
//import org.sfa.request.model.entity.Request;
//import org.sfa.request.model.entity.RequestCategory;
//import org.sfa.request.model.entity.RequestFor;
//import org.sfa.request.model.entity.RequestPriority;
//import org.sfa.request.model.entity.RequestStatus;
//import org.sfa.request.model.entity.RequestType;
//import org.sfa.request.model.enums.RequestCategoryEnum;
//import org.sfa.request.model.enums.RequestForEnum;
//import org.sfa.request.model.enums.RequestPriorityEnum;
//import org.sfa.request.model.enums.RequestStatusEnum;
//import org.sfa.request.model.enums.RequestTypeEnum;
//import org.sfa.request.repository.RequestCategoryRepository;
//import org.sfa.request.repository.RequestForRepository;
//import org.sfa.request.repository.RequestPriorityRepository;
//import org.sfa.request.repository.RequestRepository;
//import org.sfa.request.repository.RequestStatusRepository;
//import org.sfa.request.repository.RequestTypeRepository;
//import org.sfa.request.response.PagedResponse;
//import org.sfa.request.response.SaayamResponse;
//import org.sfa.request.service.api.RequestService;
//import org.sfa.request.service.api.SQSService;
//import org.sfa.request.service.impl.RequestServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
//import org.springframework.context.MessageSource;
//import org.springframework.context.NoSuchMessageException;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.aot.DisabledInAotMode;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.servlet.LocaleResolver;
//import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
//
//@ContextConfiguration(classes = {RequestController.class})
//@ExtendWith(SpringExtension.class)
//@DisabledInAotMode
//class RequestControllerDiffblueTest {
//    @MockBean
//    private LocaleResolver localeResolver;
//
//    @MockBean
//    private MessageSource messageSource;
//
//    @Autowired
//    private RequestController requestController;
//
//    @MockBean
//    private RequestService requestService;
//
//    @MockBean
//    private SQSService sQSService;
//
//    /**
//     * Method under test:
//     * {@link RequestController#createRequest(String, RequestDTO, HttpServletRequest)}
//     */
//    @Test
//    void testCreateRequest() {
//        //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
//
//        // Arrange
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request = new Request();
//        request.setAudioRequestDescription("Audio Request Description");
//        request.setCity("Oxford");
//        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setLeadVolunteerUserId(1);
//        request.setRequestCategory(requestCategory);
//        request.setRequestDescription("Request Description");
//        request.setRequestFor(requestFor);
//        request.setRequestId("42");
//        request.setRequestPriority(requestPriority);
//        request.setRequestStatus(requestStatus);
//        request.setRequestType(requestType);
//        request.setRequesterId("42");
//        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setZipCode("21654");
//        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        LocalDate ofResult = LocalDate.of(1970, 1, 1);
//        SaayamResponse<Request> buildResult = successResult.timestamp(ofResult.atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        RequestServiceImpl requestService = mock(RequestServiceImpl.class);
//        when(requestService.createRequest(Mockito.<String>any(), Mockito.<RequestDTO>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
//        SQSService sqsService = mock(SQSService.class);
//        RequestController requestController = new RequestController(requestService, localeResolver, sqsService,
//                new AnnotationConfigReactiveWebApplicationContext());
//        RequestDTO requestDTO = new RequestDTO();
//
//        // Act
//        ResponseEntity<SaayamResponse<Request>> actualCreateRequestResult = requestController.createRequest("42",
//                requestDTO, new MockHttpServletRequest());
//
//        // Assert
//        verify(requestService).createRequest(eq("42"), isA(RequestDTO.class), isA(Locale.class));
//        HttpStatusCode statusCode = actualCreateRequestResult.getStatusCode();
//        assertTrue(statusCode instanceof HttpStatus);
//        SaayamResponse<Request> body = actualCreateRequestResult.getBody();
//        ZonedDateTime timestamp = body.getTimestamp();
//        LocalDateTime toLocalDateTimeResult = timestamp.toLocalDateTime();
//        assertEquals("00:00", toLocalDateTimeResult.toLocalTime().toString());
//        LocalDate toLocalDateResult = toLocalDateTimeResult.toLocalDate();
//        assertEquals("1970-01-01", toLocalDateResult.toString());
//        assertEquals("42", requestDTO.getRequesterId());
//        assertEquals("Not all who wander are lost", body.getMessage());
//        assertEquals("Saayam Code", body.getSaayamCode());
//        assertEquals("Z", timestamp.getZone().toString());
//        assertEquals(1, body.getStatusCode());
//        assertEquals(201, actualCreateRequestResult.getStatusCodeValue());
//        assertEquals(HttpStatus.CREATED, statusCode);
//        assertTrue(body.isSuccess());
//        assertTrue(actualCreateRequestResult.hasBody());
//        assertTrue(actualCreateRequestResult.getHeaders().isEmpty());
//        assertSame(request, body.getData());
//        assertSame(ofResult, toLocalDateResult);
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#getRequestById(String, String, HttpServletRequest)}
//     */
//    @Test
//    void testGetRequestById() throws Exception {
//        // Arrange
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request = new Request();
//        request.setAudioRequestDescription("Audio Request Description");
//        request.setCity("Oxford");
//        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setLeadVolunteerUserId(1);
//        request.setRequestCategory(requestCategory);
//        request.setRequestDescription("Request Description");
//        request.setRequestFor(requestFor);
//        request.setRequestId("42");
//        request.setRequestPriority(requestPriority);
//        request.setRequestStatus(requestStatus);
//        request.setRequestType(requestType);
//        request.setRequesterId("42");
//        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setZipCode("21654");
//        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        SaayamResponse<Request> buildResult = successResult
//                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.getRequestById(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .get("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42");
//
//        // Act and Assert
//        MockMvcBuilders.standaloneSetup(requestController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(
//                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
//                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
//                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
//                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
//                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
//                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
//                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
//                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
//                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
//                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
//                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#getRequests(String, Pageable, HttpServletRequest)}
//     */
//    @Test
//    void testGetRequests() {
//        //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
//
//        // Arrange
//        RequestServiceImpl requestService = mock(RequestServiceImpl.class);
//        SaayamResponse.SaayamResponseBuilder<PagedResponse<Request>> builderResult = SaayamResponse.builder();
//        PagedResponse<Request> pagedResponse = new PagedResponse<>();
//        SaayamResponse.SaayamResponseBuilder<PagedResponse<Request>> successResult = builderResult.data(pagedResponse)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        LocalDate ofResult = LocalDate.of(1970, 1, 1);
//        SaayamResponse<PagedResponse<Request>> buildResult = successResult
//                .timestamp(ofResult.atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.getRequests(Mockito.<String>any(), Mockito.<Pageable>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
//        SQSService sqsService = mock(SQSService.class);
//        RequestController requestController = new RequestController(requestService, localeResolver, sqsService,
//                new AnnotationConfigReactiveWebApplicationContext());
//
//        // Act
//        ResponseEntity<SaayamResponse<PagedResponse<Request>>> actualRequests = requestController.getRequests("42", null,
//                new MockHttpServletRequest());
//
//        // Assert
//        verify(requestService).getRequests(eq("42"), isNull(), isA(Locale.class));
//        HttpStatusCode statusCode = actualRequests.getStatusCode();
//        assertTrue(statusCode instanceof HttpStatus);
//        SaayamResponse<PagedResponse<Request>> body = actualRequests.getBody();
//        ZonedDateTime timestamp = body.getTimestamp();
//        LocalDateTime toLocalDateTimeResult = timestamp.toLocalDateTime();
//        assertEquals("00:00", toLocalDateTimeResult.toLocalTime().toString());
//        LocalDate toLocalDateResult = toLocalDateTimeResult.toLocalDate();
//        assertEquals("1970-01-01", toLocalDateResult.toString());
//        assertEquals("Not all who wander are lost", body.getMessage());
//        assertEquals("Saayam Code", body.getSaayamCode());
//        assertEquals("Z", timestamp.getZone().toString());
//        assertEquals(1, body.getStatusCode());
//        assertEquals(200, actualRequests.getStatusCodeValue());
//        assertEquals(HttpStatus.OK, statusCode);
//        assertTrue(body.isSuccess());
//        assertTrue(actualRequests.hasBody());
//        assertTrue(actualRequests.getHeaders().isEmpty());
//        assertSame(pagedResponse, body.getData());
//        assertSame(ofResult, toLocalDateResult);
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#updateRequest(String, String, RequestDTO, HttpServletRequest)}
//     */
//    @Test
//    void testUpdateRequest() throws NoSuchMessageException {
//        //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
//
//        // Arrange
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//        Request request = mock(Request.class);
//        doNothing().when(request).setLastUpdatedAt(Mockito.<ZonedDateTime>any());
//        when(request.getRequestStatus()).thenReturn(requestStatus);
//        Optional<Request> ofResult = Optional.of(request);
//
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus2 = new RequestStatus();
//        requestStatus2.setDescription("The characteristics of someone or something");
//        requestStatus2.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus2.setRequestStatusId(1);
//        requestStatus2.setRequests(new HashSet<>());
//        requestStatus2.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request2 = new Request();
//        request2.setAudioRequestDescription("Audio Request Description");
//        request2.setCity("Oxford");
//        request2.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request2.setLeadVolunteerUserId(1);
//        request2.setRequestCategory(requestCategory);
//        request2.setRequestDescription("Request Description");
//        request2.setRequestFor(requestFor);
//        request2.setRequestId("42");
//        request2.setRequestPriority(requestPriority);
//        request2.setRequestStatus(requestStatus2);
//        request2.setRequestType(requestType);
//        request2.setRequesterId("42");
//        request2.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request2.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request2.setZipCode("21654");
//        RequestRepository requestRepository = mock(RequestRepository.class);
//        when(requestRepository.save(Mockito.<Request>any())).thenReturn(request2);
//        when(requestRepository.findActiveByRequestIdAndRequesterId(Mockito.<String>any(), Mockito.<String>any(), anyInt()))
//                .thenReturn(ofResult);
//        MessageSource messageSource = mock(MessageSource.class);
//        when(messageSource.getMessage(Mockito.<String>any(), Mockito.<Object[]>any(), Mockito.<Locale>any()))
//                .thenReturn("Not all who wander are lost");
//        RequestServiceImpl requestService = new RequestServiceImpl(requestRepository, mock(RequestStatusRepository.class),
//                mock(RequestPriorityRepository.class), mock(RequestTypeRepository.class), mock(RequestCategoryRepository.class),
//                mock(RequestForRepository.class), messageSource);
//
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
//        SQSService sqsService = mock(SQSService.class);
//        RequestController requestController = new RequestController(requestService, localeResolver, sqsService,
//                new AnnotationConfigReactiveWebApplicationContext());
//        RequestDTO requestDTO = new RequestDTO();
//
//        // Act
//        ResponseEntity<SaayamResponse<Request>> actualUpdateRequestResult = requestController.updateRequest("42", "42",
//                requestDTO, new MockHttpServletRequest());
//
//        // Assert
//        verify(request).getRequestStatus();
//        verify(request).setLastUpdatedAt(isA(ZonedDateTime.class));
//        verify(requestRepository).findActiveByRequestIdAndRequesterId(eq("42"), eq("42"), eq(6));
//        verify(messageSource).getMessage(eq("success.requestUpdated"), isA(Object[].class), isA(Locale.class));
//        verify(requestRepository).save(isA(Request.class));
//        HttpStatusCode statusCode = actualUpdateRequestResult.getStatusCode();
//        assertTrue(statusCode instanceof HttpStatus);
//        assertEquals("42", requestDTO.getRequesterId());
//        SaayamResponse<Request> body = actualUpdateRequestResult.getBody();
//        assertEquals("Not all who wander are lost", body.getMessage());
//        assertEquals("SAAYAM-1402", body.getSaayamCode());
//        assertEquals(200, body.getStatusCode());
//        assertEquals(200, actualUpdateRequestResult.getStatusCodeValue());
//        assertEquals(HttpStatus.OK, statusCode);
//        assertTrue(body.isSuccess());
//        assertTrue(actualUpdateRequestResult.hasBody());
//        assertTrue(actualUpdateRequestResult.getHeaders().isEmpty());
//        assertSame(request2, body.getData());
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#updateRequest(String, String, RequestDTO, HttpServletRequest)}
//     */
//    @Test
//    void testUpdateRequest2() {
//        //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
//
//        // Arrange
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request = new Request();
//        request.setAudioRequestDescription("Audio Request Description");
//        request.setCity("Oxford");
//        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setLeadVolunteerUserId(1);
//        request.setRequestCategory(requestCategory);
//        request.setRequestDescription("Request Description");
//        request.setRequestFor(requestFor);
//        request.setRequestId("42");
//        request.setRequestPriority(requestPriority);
//        request.setRequestStatus(requestStatus);
//        request.setRequestType(requestType);
//        request.setRequesterId("42");
//        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setZipCode("21654");
//        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        LocalDate ofResult = LocalDate.of(1970, 1, 1);
//        SaayamResponse<Request> buildResult = successResult.timestamp(ofResult.atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        RequestService requestService = mock(RequestService.class);
//        when(requestService.updateRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<RequestDTO>any(),
//                Mockito.<Locale>any())).thenReturn(buildResult);
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
//        SQSService sqsService = mock(SQSService.class);
//        RequestController requestController = new RequestController(requestService, localeResolver, sqsService,
//                new AnnotationConfigReactiveWebApplicationContext());
//        RequestDTO requestDTO = new RequestDTO();
//
//        // Act
//        ResponseEntity<SaayamResponse<Request>> actualUpdateRequestResult = requestController.updateRequest("42", "42",
//                requestDTO, new MockHttpServletRequest());
//
//        // Assert
//        verify(requestService).updateRequest(eq("42"), eq("42"), isA(RequestDTO.class), isA(Locale.class));
//        HttpStatusCode statusCode = actualUpdateRequestResult.getStatusCode();
//        assertTrue(statusCode instanceof HttpStatus);
//        SaayamResponse<Request> body = actualUpdateRequestResult.getBody();
//        ZonedDateTime timestamp = body.getTimestamp();
//        LocalDateTime toLocalDateTimeResult = timestamp.toLocalDateTime();
//        assertEquals("00:00", toLocalDateTimeResult.toLocalTime().toString());
//        LocalDate toLocalDateResult = toLocalDateTimeResult.toLocalDate();
//        assertEquals("1970-01-01", toLocalDateResult.toString());
//        assertEquals("42", requestDTO.getRequesterId());
//        assertEquals("Not all who wander are lost", body.getMessage());
//        assertEquals("Saayam Code", body.getSaayamCode());
//        assertEquals("Z", timestamp.getZone().toString());
//        assertEquals(1, body.getStatusCode());
//        assertEquals(200, actualUpdateRequestResult.getStatusCodeValue());
//        assertEquals(HttpStatus.OK, statusCode);
//        assertTrue(body.isSuccess());
//        assertTrue(actualUpdateRequestResult.hasBody());
//        assertTrue(actualUpdateRequestResult.getHeaders().isEmpty());
//        assertSame(request, body.getData());
//        assertSame(ofResult, toLocalDateResult);
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#deleteRequest(String, String, HttpServletRequest)}
//     */
//    @Test
//    void testDeleteRequest() throws Exception {
//        // Arrange
//        SaayamResponse.SaayamResponseBuilder<Void> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Void> successResult = builderResult.data(null)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        SaayamResponse<Void> buildResult = successResult
//                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.deleteRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .delete("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42");
//
//        // Act and Assert
//        MockMvcBuilders.standaloneSetup(requestController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(
//                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
//                                        + " lost\",\"timestamp\":0.0}"));
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#deleteRequest(String, String, HttpServletRequest)}
//     */
//    @Test
//    void testDeleteRequest2() throws Exception {
//        // Arrange
//        SaayamResponse.SaayamResponseBuilder<Void> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Void> successResult = builderResult.data(null)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(false);
//        SaayamResponse<Void> buildResult = successResult
//                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.deleteRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .delete("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42");
//
//        // Act and Assert
//        MockMvcBuilders.standaloneSetup(requestController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(
//                                "{\"success\":false,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
//                                        + " lost\",\"timestamp\":0.0}"));
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#cancelRequest(String, String, HttpServletRequest)}
//     */
//    @Test
//    void testCancelRequest() throws Exception {
//        // Arrange
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request = new Request();
//        request.setAudioRequestDescription("Audio Request Description");
//        request.setCity("Oxford");
//        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setLeadVolunteerUserId(1);
//        request.setRequestCategory(requestCategory);
//        request.setRequestDescription("Request Description");
//        request.setRequestFor(requestFor);
//        request.setRequestId("42");
//        request.setRequestPriority(requestPriority);
//        request.setRequestStatus(requestStatus);
//        request.setRequestType(requestType);
//        request.setRequesterId("42");
//        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setZipCode("21654");
//        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        SaayamResponse<Request> buildResult = successResult
//                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.cancelRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/api/v1.0.0/requests/{requesterId}/{requestId}/cancel", "42", "42");
//
//        // Act and Assert
//        MockMvcBuilders.standaloneSetup(requestController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(
//                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
//                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
//                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
//                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
//                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
//                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
//                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
//                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
//                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
//                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
//                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#resumeRequest(String, String, HttpServletRequest)}
//     */
//    @Test
//    void testResumeRequest() throws Exception {
//        // Arrange
//        RequestCategory requestCategory = new RequestCategory();
//        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
//        requestCategory.setDescription("The characteristics of someone or something");
//        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestCategory.setRequestCategoryId(1);
//        requestCategory.setRequests(new HashSet<>());
//
//        RequestFor requestFor = new RequestFor();
//        requestFor.setDescription("The characteristics of someone or something");
//        requestFor.setFor(RequestForEnum.UNSPECIFIED);
//        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestFor.setRequestForId(1);
//        requestFor.setRequests(new HashSet<>());
//
//        RequestPriority requestPriority = new RequestPriority();
//        requestPriority.setDescription("The characteristics of someone or something");
//        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
//        requestPriority.setPriorityId(1);
//        requestPriority.setRequests(new HashSet<>());
//
//        RequestStatus requestStatus = new RequestStatus();
//        requestStatus.setDescription("The characteristics of someone or something");
//        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestStatus.setRequestStatusId(1);
//        requestStatus.setRequests(new HashSet<>());
//        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);
//
//        RequestType requestType = new RequestType();
//        requestType.setDescription("The characteristics of someone or something");
//        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestType.setRequestTypeId(1);
//        requestType.setRequests(new HashSet<>());
//        requestType.setType(RequestTypeEnum.UNSPECIFIED);
//
//        Request request = new Request();
//        request.setAudioRequestDescription("Audio Request Description");
//        request.setCity("Oxford");
//        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setLeadVolunteerUserId(1);
//        request.setRequestCategory(requestCategory);
//        request.setRequestDescription("Request Description");
//        request.setRequestFor(requestFor);
//        request.setRequestId("42");
//        request.setRequestPriority(requestPriority);
//        request.setRequestStatus(requestStatus);
//        request.setRequestType(requestType);
//        request.setRequesterId("42");
//        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        request.setZipCode("21654");
//        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
//        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
//                .message("Not all who wander are lost")
//                .saayamCode("Saayam Code")
//                .statusCode(1)
//                .success(true);
//        SaayamResponse<Request> buildResult = successResult
//                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
//                .build();
//        when(requestService.resumeRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
//                .thenReturn(buildResult);
//        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/api/v1.0.0/requests/{requesterId}/{requestId}/resume", "42", "42");
//
//        // Act and Assert
//        MockMvcBuilders.standaloneSetup(requestController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(
//                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
//                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
//                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
//                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
//                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
//                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
//                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
//                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
//                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
//                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
//                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#sendRequestToQueue(String, String, HttpServletRequest)}
//     */
//    @Test
//    @Disabled("TODO: Complete this test")
//    void testSendRequestToQueue() {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Reason: No inputs found that don't throw a trivial exception.
//        //   Diffblue Cover tried to run the arrange/act section, but the method under
//        //   test threw
//        //   jakarta.servlet.ServletException: Request processing failed: org.springframework.context.NoSuchMessageException: No message found under code 'success.requestSentToQueue' for locale 'zh_CN'.
//        //       at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590)
//        //       at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
//        //   org.springframework.context.NoSuchMessageException: No message found under code 'success.requestSentToQueue' for locale 'zh_CN'.
//        //       at org.sfa.request.controller.RequestController.sendRequestToQueue(RequestController.java:138)
//        //       at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590)
//        //       at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
//        //   See https://diff.blue/R013 to resolve this issue.
//
//        // Arrange
//        RequestRepository requestRepository = mock(RequestRepository.class);
//        Optional<Request> ofResult = Optional.of(mock(Request.class));
//        when(requestRepository.findActiveByRequestIdAndRequesterId(Mockito.<String>any(), Mockito.<String>any(), anyInt()))
//                .thenReturn(ofResult);
//        RequestStatusRepository requestStatusRepository = mock(RequestStatusRepository.class);
//        RequestPriorityRepository requestPriorityRepository = mock(RequestPriorityRepository.class);
//        RequestTypeRepository requestTypeRepository = mock(RequestTypeRepository.class);
//        RequestCategoryRepository requestCategoryRepository = mock(RequestCategoryRepository.class);
//        RequestForRepository requestForRepository = mock(RequestForRepository.class);
//        RequestServiceImpl requestService = new RequestServiceImpl(requestRepository, requestStatusRepository,
//                requestPriorityRepository, requestTypeRepository, requestCategoryRepository, requestForRepository,
//                new AnnotationConfigReactiveWebApplicationContext());
//
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
//        SQSService sqsService = mock(SQSService.class);
//        RequestController requestController = new RequestController(requestService, localeResolver, sqsService,
//                new AnnotationConfigReactiveWebApplicationContext());
//
//        // Act
//        requestController.sendRequestToQueue("42", "42", new MockHttpServletRequest());
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#createRequest(String, RequestDTO, HttpServletRequest)}
//     */
//    @Test
//    @Disabled("TODO: Complete this test")
//    void testCreateRequest2() throws Exception {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Reason: No inputs found that don't throw a trivial exception.
//        //   Diffblue Cover tried to run the arrange/act section, but the method under
//        //   test threw
//        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.ZonedDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: org.sfa.request.dto.RequestDTO["submittedAt"])
//        //       at com.fasterxml.jackson.databind.exc.InvalidDefinitionException.from(InvalidDefinitionException.java:77)
//        //       at com.fasterxml.jackson.databind.SerializerProvider.reportBadDefinition(SerializerProvider.java:1330)
//        //       at com.fasterxml.jackson.databind.ser.impl.UnsupportedTypeSerializer.serialize(UnsupportedTypeSerializer.java:35)
//        //       at com.fasterxml.jackson.databind.ser.BeanPropertyWriter.serializeAsField(BeanPropertyWriter.java:732)
//        //       at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:770)
//        //       at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:183)
//        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider._serialize(DefaultSerializerProvider.java:502)
//        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.serializeValue(DefaultSerializerProvider.java:341)
//        //       at com.fasterxml.jackson.databind.ObjectMapper._writeValueAndClose(ObjectMapper.java:4799)
//        //       at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:4040)
//        //   See https://diff.blue/R013 to resolve this issue.
//
//        // Arrange
//        RequestDTO requestDTO = new RequestDTO();
//        requestDTO.setAudioRequestDescription("Audio Request Description");
//        requestDTO.setCity("Oxford");
//        requestDTO.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setLeadVolunteerUserId(1);
//        requestDTO.setRequestCategory(new RequestCategoryDTO());
//        requestDTO.setRequestDescription("Request Description");
//        requestDTO.setRequestFor(new RequestForDTO());
//        requestDTO.setRequestPriority(new RequestPriorityDTO());
//        requestDTO.setRequestStatus(new RequestStatusDTO());
//        requestDTO.setRequestType(new RequestTypeDTO());
//        requestDTO.setRequesterId("42");
//        requestDTO.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setZipCode("21654");
//        String content = (new ObjectMapper()).writeValueAsString(requestDTO);
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/api/v1.0.0/requests/{requesterId}", "42")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(content);
//
//        // Act
//        MockMvcBuilders.standaloneSetup(requestController).build().perform(requestBuilder);
//    }
//
//    /**
//     * Method under test:
//     * {@link RequestController#updateRequest(String, String, RequestDTO, HttpServletRequest)}
//     */
//    @Test
//    @Disabled("TODO: Complete this test")
//    void testUpdateRequest3() throws Exception {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Reason: No inputs found that don't throw a trivial exception.
//        //   Diffblue Cover tried to run the arrange/act section, but the method under
//        //   test threw
//        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.ZonedDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: org.sfa.request.dto.RequestDTO["submittedAt"])
//        //       at com.fasterxml.jackson.databind.exc.InvalidDefinitionException.from(InvalidDefinitionException.java:77)
//        //       at com.fasterxml.jackson.databind.SerializerProvider.reportBadDefinition(SerializerProvider.java:1330)
//        //       at com.fasterxml.jackson.databind.ser.impl.UnsupportedTypeSerializer.serialize(UnsupportedTypeSerializer.java:35)
//        //       at com.fasterxml.jackson.databind.ser.BeanPropertyWriter.serializeAsField(BeanPropertyWriter.java:732)
//        //       at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:770)
//        //       at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:183)
//        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider._serialize(DefaultSerializerProvider.java:502)
//        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.serializeValue(DefaultSerializerProvider.java:341)
//        //       at com.fasterxml.jackson.databind.ObjectMapper._writeValueAndClose(ObjectMapper.java:4799)
//        //       at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:4040)
//        //   See https://diff.blue/R013 to resolve this issue.
//
//        // Arrange
//        RequestDTO requestDTO = new RequestDTO();
//        requestDTO.setAudioRequestDescription("Audio Request Description");
//        requestDTO.setCity("Oxford");
//        requestDTO.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setLeadVolunteerUserId(1);
//        requestDTO.setRequestCategory(new RequestCategoryDTO());
//        requestDTO.setRequestDescription("Request Description");
//        requestDTO.setRequestFor(new RequestForDTO());
//        requestDTO.setRequestPriority(new RequestPriorityDTO());
//        requestDTO.setRequestStatus(new RequestStatusDTO());
//        requestDTO.setRequestType(new RequestTypeDTO());
//        requestDTO.setRequesterId("42");
//        requestDTO.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
//        requestDTO.setZipCode("21654");
//        String content = (new ObjectMapper()).writeValueAsString(requestDTO);
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
//                .put("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(content);
//
//        // Act
//        MockMvcBuilders.standaloneSetup(requestController).build().perform(requestBuilder);
//    }
//}
