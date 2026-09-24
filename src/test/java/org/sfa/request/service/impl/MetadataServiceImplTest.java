package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sfa.request.dto.ReqAddInfoDTO;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.sfa.request.repository.ListItemMetadataRepository;
import org.sfa.request.repository.ReqAddInfoMetadataRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataServiceImplTest {

    @Mock
    private ReqAddInfoMetadataRepository metadataRepository;

    @Mock
    private ListItemMetadataRepository listItemMetadataRepository;

    @InjectMocks
    private MetadataServiceImpl metadataService;

    @Test
    void getMetadataFormByCategoryId_returnsActiveFieldsOnly() {
        ReqAddInfoMetadata activeMetadata = ReqAddInfoMetadata.builder()
                .fieldId("5.2.A")
                .fieldNameKey("DELIVERY_TYPE")
                .fieldType("list")
                .status("active")
                .catId("5.2")
                .build();
        when(metadataRepository.findByCatIdAndStatus("5.2", "active"))
                .thenReturn(new ArrayList<>(List.of(activeMetadata)));
        when(listItemMetadataRepository.findByFieldIdIn(List.of("5.2.A")))
                .thenReturn(new ArrayList<>());

        ReqAddInfoMetadataTreeDto result = metadataService.getMetadataFormByCategoryId("5.2");

        assertEquals("5.2", result.getCatId());
        assertEquals(1, result.getFields().size());
        assertEquals("5.2.A", result.getFields().get(0).getFieldId());
        assertEquals("active", result.getFields().get(0).getStatus());
        verify(metadataRepository, never()).findByCatId("5.2");
    }

    @Test
    void getMetadataByCategoryId_usesActiveOnlyQuery() {
        ReqAddInfoMetadata activeMetadata = metadata("5.2.A", "active", "5.2");
        when(metadataRepository.findByCatIdAndStatus("5.2", "active"))
                .thenReturn(new ArrayList<>(List.of(activeMetadata)));

        var result = metadataService.getMetadataByCategoryId("5.2");

        assertEquals(1, result.size());
        assertEquals("5.2.A", result.get(0).getFieldId());
        verify(metadataRepository, never()).findByCatId("5.2");
    }

    @Test
    void getMetadataCategoryTree_filtersInactiveFields() {
        ReqAddInfoMetadata activeMetadata = metadata("5.2.A", "active", "5.2");
        ReqAddInfoMetadata inactiveMetadata = metadata("5.2.B", "inactive", "5.2");
        when(metadataRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(activeMetadata, inactiveMetadata)));

        var result = metadataService.getMetadataCategoryTree();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getFields().size());
        assertEquals("5.2.A", result.get(0).getFields().get(0).getFieldId());
    }

    @Test
    void requestDtoToString_excludesAdditionalFieldsContent() {
        RequestDTO request = new RequestDTO();
        request.setAdditionalFields(List.of(
                ReqAddInfoDTO.builder()
                        .fieldId("5.2.SECRET_FIELD")
                        .fieldValue("SECRET_SCALAR_VALUE")
                        .selectedItems(List.of("5.2.SECRET_ITEM"))
                        .build()
        ));

        String requestText = request.toString();

        assertFalse(requestText.contains("5.2.SECRET_FIELD"));
        assertFalse(requestText.contains("SECRET_SCALAR_VALUE"));
        assertFalse(requestText.contains("5.2.SECRET_ITEM"));
    }

    private ReqAddInfoMetadata metadata(String fieldId, String status, String categoryId) {
        return ReqAddInfoMetadata.builder()
                .fieldId(fieldId)
                .fieldNameKey(fieldId)
                .fieldType("list")
                .status(status)
                .catId(categoryId)
                .build();
    }
}
