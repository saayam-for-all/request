package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.sfa.request.dto.*;
import org.sfa.request.model.entity.*;
import org.sfa.request.model.enums.*;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Locale;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RequestController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class RequestControllerTest {
    @MockBean
    private LocaleResolver localeResolver;

    @Autowired
    private RequestController requestController;

    @MockBean
    private RequestService requestService;

    /**
     * Method under test:
     * {@link RequestController#getRequestById(String, String, HttpServletRequest)}
     */
    @Test
    void testGetRequestById() throws Exception {
        // Arrange
        RequestCategory requestCategory = new RequestCategory();
        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory.setDescription("The characteristics of someone or something");
        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestCategory.setRequestCategoryId(1);
        requestCategory.setRequests(new HashSet<>());

        RequestFor requestFor = new RequestFor();
        requestFor.setDescription("The characteristics of someone or something");
        requestFor.setFor(RequestForEnum.UNSPECIFIED);
        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestFor.setRequestForId(1);
        requestFor.setRequests(new HashSet<>());

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setDescription("The characteristics of someone or something");
        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
        requestPriority.setPriorityId(1);
        requestPriority.setRequests(new HashSet<>());

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setDescription("The characteristics of someone or something");
        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestStatus.setRequestStatusId(1);
        requestStatus.setRequests(new HashSet<>());
        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);

        RequestType requestType = new RequestType();
        requestType.setDescription("The characteristics of someone or something");
        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestType.setRequestTypeId(1);
        requestType.setRequests(new HashSet<>());
        requestType.setType(RequestTypeEnum.UNSPECIFIED);

        Request request = new Request();
        request.setAudioRequestDescription("Audio Request Description");
        request.setCity("Oxford");
        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setLeadVolunteerUserId(1);
        request.setRequestCategory(requestCategory);
        request.setRequestDescription("Request Description");
        request.setRequestFor(requestFor);
        request.setRequestId("42");
        request.setRequestPriority(requestPriority);
        request.setRequestStatus(requestStatus);
        request.setRequestType(requestType);
        request.setRequesterId("42");
        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setZipCode("21654");
        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
                .message("Not all who wander are lost")
                .saayamCode("Saayam Code")
                .statusCode(1)
                .success(true);
        SaayamResponse<Request> buildResult = successResult
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.getRequestById(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
                .thenReturn(buildResult);
        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
    }

    /**
     * Method under test:
     * {@link RequestController#getRequests(String, Pageable, HttpServletRequest)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetRequests() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: Can't find a suitable constructor.
        //   Diffblue Cover was unable to construct an instance of Pageable.
        //   No suitable constructor or factory method found. Please check that the class
        //   under test has a non-private constructor or factory method.
        //   See https://diff.blue/R083 for further troubleshooting of this issue.

        // Arrange
        // TODO: Populate arranged inputs
        RequestController requestController = null;
        String requesterId = "";
        Pageable pageable = null;
        HttpServletRequest request = null;

        // Act
        ResponseEntity<SaayamResponse<PagedResponse<Request>>> actualRequests = requestController.getRequests(requesterId,
                pageable, request);

        // Assert
        // TODO: Add assertions on result
    }

    /**
     * Method under test:
     * {@link RequestController#deleteRequest(String, String, HttpServletRequest)}
     */
    @Test
    void testDeleteRequest() throws Exception {
        // Arrange
        SaayamResponse.SaayamResponseBuilder<Void> builderResult = SaayamResponse.builder();
        SaayamResponse.SaayamResponseBuilder<Void> successResult = builderResult.data(null)
                .message("Not all who wander are lost")
                .saayamCode("Saayam Code")
                .statusCode(1)
                .success(true);
        SaayamResponse<Void> buildResult = successResult
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.deleteRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
                .thenReturn(buildResult);
        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
                                        + " lost\",\"timestamp\":0.0}"));
    }

    /**
     * Method under test:
     * {@link RequestController#cancelRequest(String, String, HttpServletRequest)}
     */
    @Test
    void testCancelRequest() throws Exception {
        // Arrange
        RequestCategory requestCategory = new RequestCategory();
        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory.setDescription("The characteristics of someone or something");
        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestCategory.setRequestCategoryId(1);
        requestCategory.setRequests(new HashSet<>());

        RequestFor requestFor = new RequestFor();
        requestFor.setDescription("The characteristics of someone or something");
        requestFor.setFor(RequestForEnum.UNSPECIFIED);
        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestFor.setRequestForId(1);
        requestFor.setRequests(new HashSet<>());

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setDescription("The characteristics of someone or something");
        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
        requestPriority.setPriorityId(1);
        requestPriority.setRequests(new HashSet<>());

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setDescription("The characteristics of someone or something");
        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestStatus.setRequestStatusId(1);
        requestStatus.setRequests(new HashSet<>());
        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);

        RequestType requestType = new RequestType();
        requestType.setDescription("The characteristics of someone or something");
        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestType.setRequestTypeId(1);
        requestType.setRequests(new HashSet<>());
        requestType.setType(RequestTypeEnum.UNSPECIFIED);

        Request request = new Request();
        request.setAudioRequestDescription("Audio Request Description");
        request.setCity("Oxford");
        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setLeadVolunteerUserId(1);
        request.setRequestCategory(requestCategory);
        request.setRequestDescription("Request Description");
        request.setRequestFor(requestFor);
        request.setRequestId("42");
        request.setRequestPriority(requestPriority);
        request.setRequestStatus(requestStatus);
        request.setRequestType(requestType);
        request.setRequesterId("42");
        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setZipCode("21654");
        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
                .message("Not all who wander are lost")
                .saayamCode("Saayam Code")
                .statusCode(1)
                .success(true);
        SaayamResponse<Request> buildResult = successResult
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.cancelRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
                .thenReturn(buildResult);
        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/v1.0.0/requests/{requesterId}/{requestId}/cancel", "42", "42");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
    }

    /**
     * Method under test:
     * {@link RequestController#resumeRequest(String, String, HttpServletRequest)}
     */
    @Test
    void testResumeRequest() throws Exception {
        // Arrange
        RequestCategory requestCategory = new RequestCategory();
        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory.setDescription("The characteristics of someone or something");
        requestCategory.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestCategory.setRequestCategoryId(1);
        requestCategory.setRequests(new HashSet<>());

        RequestFor requestFor = new RequestFor();
        requestFor.setDescription("The characteristics of someone or something");
        requestFor.setFor(RequestForEnum.UNSPECIFIED);
        requestFor.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestFor.setRequestForId(1);
        requestFor.setRequests(new HashSet<>());

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setDescription("The characteristics of someone or something");
        requestPriority.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestPriority.setPriority(RequestPriorityEnum.UNSPECIFIED);
        requestPriority.setPriorityId(1);
        requestPriority.setRequests(new HashSet<>());

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setDescription("The characteristics of someone or something");
        requestStatus.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestStatus.setRequestStatusId(1);
        requestStatus.setRequests(new HashSet<>());
        requestStatus.setStatus(RequestStatusEnum.UNSPECIFIED);

        RequestType requestType = new RequestType();
        requestType.setDescription("The characteristics of someone or something");
        requestType.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestType.setRequestTypeId(1);
        requestType.setRequests(new HashSet<>());
        requestType.setType(RequestTypeEnum.UNSPECIFIED);

        Request request = new Request();
        request.setAudioRequestDescription("Audio Request Description");
        request.setCity("Oxford");
        request.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setLeadVolunteerUserId(1);
        request.setRequestCategory(requestCategory);
        request.setRequestDescription("Request Description");
        request.setRequestFor(requestFor);
        request.setRequestId("42");
        request.setRequestPriority(requestPriority);
        request.setRequestStatus(requestStatus);
        request.setRequestType(requestType);
        request.setRequesterId("42");
        request.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        request.setZipCode("21654");
        SaayamResponse.SaayamResponseBuilder<Request> builderResult = SaayamResponse.builder();
        SaayamResponse.SaayamResponseBuilder<Request> successResult = builderResult.data(request)
                .message("Not all who wander are lost")
                .saayamCode("Saayam Code")
                .statusCode(1)
                .success(true);
        SaayamResponse<Request> buildResult = successResult
                .timestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC))
                .build();
        when(requestService.resumeRequest(Mockito.<String>any(), Mockito.<String>any(), Mockito.<Locale>any()))
                .thenReturn(buildResult);
        when(localeResolver.resolveLocale(Mockito.<HttpServletRequest>any())).thenReturn(Locale.getDefault());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/v1.0.0/requests/{requesterId}/{requestId}/resume", "42", "42");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(requestController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"success\":true,\"statusCode\":1,\"saayamCode\":\"Saayam Code\",\"message\":\"Not all who wander are"
                                        + " lost\",\"data\":{\"requestId\":\"42\",\"requesterId\":\"42\",\"requestStatus\":{\"requestStatusId\":1,\"status\":"
                                        + "\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt\":0.0},"
                                        + "\"requestPriority\":{\"priorityId\":1,\"priority\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone"
                                        + " or something\",\"lastUpdatedAt\":0.0},\"requestType\":{\"requestTypeId\":1,\"type\":\"UNSPECIFIED\",\"description\":\"The"
                                        + " characteristics of someone or something\",\"lastUpdatedAt\":0.0},\"requestCategory\":{\"requestCategoryId"
                                        + "\":1,\"category\":\"UNSPECIFIED\",\"description\":\"The characteristics of someone or something\",\"lastUpdatedAt"
                                        + "\":0.0},\"requestFor\":{\"requestForId\":1,\"description\":\"The characteristics of someone or something\","
                                        + "\"lastUpdatedAt\":0.0,\"for\":\"UNSPECIFIED\"},\"city\":\"Oxford\",\"zipCode\":\"21654\",\"requestDescription\":\"Request"
                                        + " Description\",\"audioRequestDescription\":\"Audio Request Description\",\"submittedAt\":0.0,\"leadVolunteerUserId"
                                        + "\":1,\"servicedAt\":0.0,\"lastUpdatedAt\":0.0},\"timestamp\":0.0}"));
    }

    /**
     * Method under test:
     * {@link RequestController#createRequest(String, RequestDTO, HttpServletRequest)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testCreateRequest() throws Exception {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.ZonedDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: org.sfa.request.dto.RequestDTO["submittedAt"])
        //       at com.fasterxml.jackson.databind.exc.InvalidDefinitionException.from(InvalidDefinitionException.java:77)
        //       at com.fasterxml.jackson.databind.SerializerProvider.reportBadDefinition(SerializerProvider.java:1330)
        //       at com.fasterxml.jackson.databind.ser.impl.UnsupportedTypeSerializer.serialize(UnsupportedTypeSerializer.java:35)
        //       at com.fasterxml.jackson.databind.ser.BeanPropertyWriter.serializeAsField(BeanPropertyWriter.java:732)
        //       at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:770)
        //       at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:183)
        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider._serialize(DefaultSerializerProvider.java:502)
        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.serializeValue(DefaultSerializerProvider.java:341)
        //       at com.fasterxml.jackson.databind.ObjectMapper._writeValueAndClose(ObjectMapper.java:4799)
        //       at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:4040)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setAudioRequestDescription("Audio Request Description");
        requestDTO.setCity("Oxford");
        requestDTO.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setLeadVolunteerUserId(1);
        requestDTO.setRequestCategory(new RequestCategoryDTO());
        requestDTO.setRequestDescription("Request Description");
        requestDTO.setRequestFor(new RequestForDTO());
        requestDTO.setRequestPriority(new RequestPriorityDTO());
        requestDTO.setRequestStatus(new RequestStatusDTO());
        requestDTO.setRequestType(new RequestTypeDTO());
        requestDTO.setRequesterId("42");
        requestDTO.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setZipCode("21654");
        String content = (new ObjectMapper()).writeValueAsString(requestDTO);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/v1.0.0/requests/{requesterId}", "42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        MockMvcBuilders.standaloneSetup(requestController).build().perform(requestBuilder);
    }

    /**
     * Method under test:
     * {@link RequestController#updateRequest(String, String, RequestDTO, HttpServletRequest)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testUpdateRequest() throws Exception {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.ZonedDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: org.sfa.request.dto.RequestDTO["submittedAt"])
        //       at com.fasterxml.jackson.databind.exc.InvalidDefinitionException.from(InvalidDefinitionException.java:77)
        //       at com.fasterxml.jackson.databind.SerializerProvider.reportBadDefinition(SerializerProvider.java:1330)
        //       at com.fasterxml.jackson.databind.ser.impl.UnsupportedTypeSerializer.serialize(UnsupportedTypeSerializer.java:35)
        //       at com.fasterxml.jackson.databind.ser.BeanPropertyWriter.serializeAsField(BeanPropertyWriter.java:732)
        //       at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:770)
        //       at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:183)
        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider._serialize(DefaultSerializerProvider.java:502)
        //       at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.serializeValue(DefaultSerializerProvider.java:341)
        //       at com.fasterxml.jackson.databind.ObjectMapper._writeValueAndClose(ObjectMapper.java:4799)
        //       at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:4040)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setAudioRequestDescription("Audio Request Description");
        requestDTO.setCity("Oxford");
        requestDTO.setLastUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setLeadVolunteerUserId(1);
        requestDTO.setRequestCategory(new RequestCategoryDTO());
        requestDTO.setRequestDescription("Request Description");
        requestDTO.setRequestFor(new RequestForDTO());
        requestDTO.setRequestPriority(new RequestPriorityDTO());
        requestDTO.setRequestStatus(new RequestStatusDTO());
        requestDTO.setRequestType(new RequestTypeDTO());
        requestDTO.setRequesterId("42");
        requestDTO.setServicedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setSubmittedAt(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC));
        requestDTO.setZipCode("21654");
        String content = (new ObjectMapper()).writeValueAsString(requestDTO);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/v1.0.0/requests/{requesterId}/{requestId}", "42", "42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        MockMvcBuilders.standaloneSetup(requestController).build().perform(requestBuilder);
    }
}
