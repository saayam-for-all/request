package org.sfa.request.controller;

import org.sfa.request.dto.ReqAddInfoMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.service.api.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/metadata","/dev/requests/v0.0.1/metadata"})
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/category/{catId}/fields")
    public ResponseEntity<List<ReqAddInfoMetadataDto>> getFieldsByCatId(@PathVariable String catId) {
        List<ReqAddInfoMetadataDto> dtoList = metadataService.getMetadataByCategoryId(catId).stream()
                .map(metadata -> {
                    ReqAddInfoMetadataDto dto = new ReqAddInfoMetadataDto();
                    dto.setFieldId(metadata.getFieldId());
                    dto.setFieldNameKey(metadata.getFieldNameKey());
                    dto.setFieldType(metadata.getFieldType());
                    dto.setStatus(metadata.getStatus());
                    dto.setCatId(metadata.getCatId());
                    return dto;
                }).toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/category/tree")
    public ResponseEntity<Map<String, List<ReqAddInfoMetadataTreeDto>>> getGroupedMetadataTree() {
        return ResponseEntity.ok(metadataService.getFullMetadataTree());
    }
}
