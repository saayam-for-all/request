package org.sfa.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.ReqAddInfoDTO;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestForDTO;
import org.sfa.request.dto.RequestPriorityDTO;
import org.sfa.request.dto.RequestStatusDTO;
import org.sfa.request.dto.RequestTypeDTO;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.entity.ListItemMetadata;
import org.sfa.request.model.entity.ReqAddInfo;
import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.repository.HelpCategoryRepository;
import org.sfa.request.repository.ListItemMetadataRepository;
import org.sfa.request.repository.ReqAddInfoMetadataRepository;
import org.sfa.request.repository.ReqAddInfoRepository;
import org.sfa.request.repository.RequestForRepository;
import org.sfa.request.repository.RequestGuestDetailsRepository;
import org.sfa.request.repository.RequestIsLeadVolunteerRepository;
import org.sfa.request.repository.RequestPriorityRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.RequestTypeRepository;
import org.sfa.request.service.api.RequestService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplAdditionalInfoTest {

    private static final Locale LOCALE = Locale.US;
    private static final String CATEGORY_ID = "5.2";

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
    private ReqAddInfoMetadataRepository reqAddInfoMetadataRepository;
    @Mock
    private ListItemMetadataRepository listItemMetadataRepository;
    @Mock
    private ReqAddInfoRepository reqAddInfoRepository;
    @Mock
    private RequestForRepository requestForRepository;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        RequestPriority priority = new RequestPriority();
        priority.setPriorityId(1);
        when(requestPriorityRepository.findById(1)).thenReturn(Optional.of(priority));

        RequestType type = new RequestType();
        type.setRequestTypeId(1);
        when(requestTypeRepository.findById(1)).thenReturn(Optional.of(type));

        when(helpCategoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(
                HelpCategory.builder().catId(CATEGORY_ID).catName("MEDICINE_DELIVERY").build()
        ));

        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(0);
        when(requestForRepository.findById(0)).thenReturn(Optional.of(requestFor));

        RequestStatus status = new RequestStatus();
        status.setRequestStatusId(1);
        when(requestStatusRepository.findById(anyInt())).thenReturn(Optional.of(status));

        RequestIsLeadVolunteer isLeadVolunteer = new RequestIsLeadVolunteer();
        isLeadVolunteer.setReqIsleadId(0);
        when(requestIsLeadVolunteerRepository.findById(0)).thenReturn(Optional.of(isLeadVolunteer));

        lenient().when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request request = invocation.getArgument(0);
            request.setRequestId("REQ-00-000-000-0029");
            return request;
        });
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("created");
    }

    @Test
    void createRequest_savesValidHealthcareAdditionalInfo() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                ReqAddInfoDTO.builder()
                        .fieldId("5.2.A")
                        .selectedItems(List.of("5.2.A.1"))
                        .build(),
                ReqAddInfoDTO.builder()
                        .fieldId("5.2.E")
                        .fieldValue("true")
                        .build()
        ));
        when(reqAddInfoMetadataRepository.findById("5.2.A"))
                .thenReturn(Optional.of(metadata("5.2.A", "list", "active", CATEGORY_ID)));
        when(reqAddInfoMetadataRepository.findById("5.2.E"))
                .thenReturn(Optional.of(metadata("5.2.E", "checkbox", "active", CATEGORY_ID)));
        when(listItemMetadataRepository.findByFieldId("5.2.A"))
                .thenReturn(List.of(listItem("5.2.A.1", "5.2.A")));

        requestService.createRequest("SID-29", request, LOCALE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReqAddInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(reqAddInfoRepository).saveAll(captor.capture());
        List<ReqAddInfo> savedRows = captor.getValue();
        assertEquals(2, savedRows.size());
        assertEquals("REQ-00-000-000-0029", savedRows.get(0).getRequestId());
        assertEquals("5.2.A", savedRows.get(0).getFieldId());
        assertEquals("5.2.A.1", savedRows.get(0).getItemId());
        assertNull(savedRows.get(0).getFieldValue());
        assertEquals("5.2.E", savedRows.get(1).getFieldId());
        assertNull(savedRows.get(1).getItemId());
        assertEquals("true", savedRows.get(1).getFieldValue());
    }

    @Test
    void createRequest_acceptsMissingAdditionalInfoForBackwardCompatibility() {
        requestService.createRequest("SID-29", requestWithAdditionalFields(null), LOCALE);

        verify(reqAddInfoRepository, never()).saveAll(any());
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsUnknownField() {
        RequestDTO request = requestWithAdditionalFields(List.of(scalarField("5.2.UNKNOWN", "value")));
        when(reqAddInfoMetadataRepository.findById("5.2.UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsCrossCategoryField() {
        RequestDTO request = requestWithAdditionalFields(List.of(scalarField("5.3.C", "value")));
        when(reqAddInfoMetadataRepository.findById("5.3.C"))
                .thenReturn(Optional.of(metadata("5.3.C", "textbox", "active", "5.3")));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsInactiveMetadata() {
        RequestDTO request = requestWithAdditionalFields(List.of(scalarField("5.2.E", "true")));
        when(reqAddInfoMetadataRepository.findById("5.2.E"))
                .thenReturn(Optional.of(metadata("5.2.E", "checkbox", "inactive", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsFieldTypeMismatch() {
        RequestDTO request = requestWithAdditionalFields(List.of(scalarField("5.2.E", "not-a-boolean")));
        when(reqAddInfoMetadataRepository.findById("5.2.E"))
                .thenReturn(Optional.of(metadata("5.2.E", "checkbox", "active", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsEmptySelectedItemsForScalarField() {
        assertRejectedScalarPayload(
                ReqAddInfoDTO.builder().fieldId("5.2.E").fieldValue("true").selectedItems(List.of()).build(),
                "checkbox"
        );
    }

    @Test
    void createRequest_rejectsNonEmptySelectedItemsForScalarField() {
        assertRejectedScalarPayload(
                ReqAddInfoDTO.builder().fieldId("5.2.E").fieldValue("true")
                        .selectedItems(List.of("5.2.E.1")).build(),
                "checkbox"
        );
    }

    @Test
    void createRequest_rejectsNullScalarValue() {
        assertRejectedScalarPayload(scalarField("5.2.E", null), "checkbox");
    }

    @Test
    void createRequest_rejectsBlankScalarValue() {
        assertRejectedScalarPayload(scalarField("5.2.E", "   "), "checkbox");
    }

    @Test
    void createRequest_rejectsBlankScalarValueForListField() {
        assertRejectedListPayload(
                ReqAddInfoDTO.builder().fieldId("5.2.A").fieldValue("")
                        .selectedItems(List.of("5.2.A.1")).build()
        );
    }

    @Test
    void createRequest_rejectsNonBlankScalarValueForListField() {
        assertRejectedListPayload(
                ReqAddInfoDTO.builder().fieldId("5.2.A").fieldValue("display value")
                        .selectedItems(List.of("5.2.A.1")).build()
        );
    }

    @Test
    void createRequest_rejectsNullSelectedItemsForListField() {
        assertRejectedListPayload(ReqAddInfoDTO.builder().fieldId("5.2.A").build());
    }

    @Test
    void createRequest_rejectsEmptySelectedItemsForListField() {
        assertRejectedListPayload(
                ReqAddInfoDTO.builder().fieldId("5.2.A").selectedItems(List.of()).build()
        );
    }

    @Test
    void createRequest_rejectsMalformedInteger() {
        assertRejectedScalarPayload(scalarField("5.2.INTEGER", "12.5"), "integer");
    }

    @Test
    void createRequest_rejectsMalformedCurrency() {
        assertRejectedScalarPayload(scalarField("5.2.CURRENCY", "twelve dollars"), "currency");
    }

    @Test
    void createRequest_rejectsMalformedTime() {
        assertRejectedScalarPayload(scalarField("5.2.TIME", "25:61"), "time");
    }

    @Test
    void createRequest_rejectsMalformedDateTime() {
        assertRejectedScalarPayload(scalarField("5.2.DATE_TIME", "not-a-date"), "date&time");
    }

    @Test
    void createRequest_rejectsNullMetadataFieldType() {
        assertRejectedScalarPayload(scalarField("5.2.NULL_TYPE", "value"), null);
    }

    @Test
    void createRequest_rejectsUnsupportedMetadataFieldType() {
        assertRejectedScalarPayload(scalarField("5.2.UNSUPPORTED", "value"), "unsupported");
    }

    @Test
    void createRequest_rejectsNullSelectedItem() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                ReqAddInfoDTO.builder().fieldId("5.2.A")
                        .selectedItems(Collections.singletonList(null)).build()
        ));
        when(reqAddInfoMetadataRepository.findById("5.2.A"))
                .thenReturn(Optional.of(metadata("5.2.A", "list", "active", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsBlankFieldId() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                ReqAddInfoDTO.builder().fieldId("   ").fieldValue("value").build()
        ));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsInvalidListItem() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                ReqAddInfoDTO.builder().fieldId("5.2.A").selectedItems(List.of("5.2.A.99")).build()
        ));
        when(reqAddInfoMetadataRepository.findById("5.2.A"))
                .thenReturn(Optional.of(metadata("5.2.A", "list", "active", CATEGORY_ID)));
        when(listItemMetadataRepository.findByFieldId("5.2.A"))
                .thenReturn(List.of(listItem("5.2.A.1", "5.2.A")));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsDuplicateFieldIds() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                scalarField("5.2.E", "true"),
                scalarField("5.2.E", "false")
        ));
        when(reqAddInfoMetadataRepository.findById("5.2.E"))
                .thenReturn(Optional.of(metadata("5.2.E", "checkbox", "active", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rejectsDuplicateSelectedItems() {
        RequestDTO request = requestWithAdditionalFields(List.of(
                ReqAddInfoDTO.builder()
                        .fieldId("5.2.A")
                        .selectedItems(List.of("5.2.A.1", "5.2.A.1"))
                        .build()
        ));
        when(reqAddInfoMetadataRepository.findById("5.2.A"))
                .thenReturn(Optional.of(metadata("5.2.A", "list", "active", CATEGORY_ID)));
        when(listItemMetadataRepository.findByFieldId("5.2.A"))
                .thenReturn(List.of(listItem("5.2.A.1", "5.2.A")));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_rollsBackWhenAdditionalInfoSaveFails() {
        RequestDTO request = requestWithAdditionalFields(List.of(scalarField("5.2.E", "true")));
        when(reqAddInfoMetadataRepository.findById("5.2.E"))
                .thenReturn(Optional.of(metadata("5.2.E", "checkbox", "active", CATEGORY_ID)));
        when(reqAddInfoRepository.saveAll(any())).thenThrow(new RuntimeException("database failure"));

        PlatformTransactionManager transactionManager = org.mockito.Mockito.mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = org.mockito.Mockito.mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        TransactionInterceptor transactionInterceptor = new TransactionInterceptor(
                transactionManager,
                new AnnotationTransactionAttributeSource()
        );
        ProxyFactory proxyFactory = new ProxyFactory(requestService);
        proxyFactory.addAdvice(transactionInterceptor);
        RequestService transactionalService = (RequestService) proxyFactory.getProxy();

        assertThrows(RuntimeException.class,
                () -> transactionalService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository).save(any(Request.class));
        verify(transactionManager).rollback(transactionStatus);
    }

    private RequestDTO requestWithAdditionalFields(List<ReqAddInfoDTO> additionalFields) {
        RequestDTO request = new RequestDTO();
        request.setRequesterId("SID-29");
        request.setRequestSubject("Medicine delivery");
        request.setRequestDescription("Need non-clinical assistance");
        request.setIsLeadVolunteer(0);

        RequestStatusDTO status = new RequestStatusDTO();
        status.setRequestStatusId(1);
        request.setRequestStatus(status);

        RequestPriorityDTO priority = new RequestPriorityDTO();
        priority.setRequestPriorityId(1);
        request.setRequestPriority(priority);

        RequestTypeDTO type = new RequestTypeDTO();
        type.setRequestTypeId(1);
        request.setRequestType(type);

        HelpCategoryDto category = new HelpCategoryDto();
        category.setCatId(CATEGORY_ID);
        request.setHelpCategory(category);

        RequestForDTO requestFor = new RequestForDTO();
        requestFor.setRequestForId(0);
        request.setRequestFor(requestFor);
        request.setAdditionalFields(additionalFields);
        return request;
    }

    private ReqAddInfoDTO scalarField(String fieldId, String value) {
        return ReqAddInfoDTO.builder().fieldId(fieldId).fieldValue(value).build();
    }

    private void assertRejectedScalarPayload(ReqAddInfoDTO submittedField, String fieldType) {
        RequestDTO request = requestWithAdditionalFields(List.of(submittedField));
        when(reqAddInfoMetadataRepository.findById(submittedField.getFieldId()))
                .thenReturn(Optional.of(metadata(submittedField.getFieldId(), fieldType, "active", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    private void assertRejectedListPayload(ReqAddInfoDTO submittedField) {
        RequestDTO request = requestWithAdditionalFields(List.of(submittedField));
        when(reqAddInfoMetadataRepository.findById(submittedField.getFieldId()))
                .thenReturn(Optional.of(metadata(submittedField.getFieldId(), "list", "active", CATEGORY_ID)));

        assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest("SID-29", request, LOCALE));
        verify(requestRepository, never()).save(any(Request.class));
    }

    private ReqAddInfoMetadata metadata(String fieldId, String fieldType, String status, String categoryId) {
        return ReqAddInfoMetadata.builder()
                .fieldId(fieldId)
                .fieldType(fieldType)
                .status(status)
                .catId(categoryId)
                .build();
    }

    private ListItemMetadata listItem(String itemId, String fieldId) {
        return ListItemMetadata.builder().itemId(itemId).fieldId(fieldId).build();
    }
}
