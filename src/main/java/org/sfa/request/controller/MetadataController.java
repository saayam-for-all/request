package org.sfa.request.controller;

import org.sfa.request.dto.MetadataFormRequestDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.service.api.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/metadata", "/dev/requests/v0.0.1/metadata"})
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("/form")
    public ResponseEntity<ReqAddInfoMetadataTreeDto> getMetadataForm(@RequestBody MetadataFormRequestDto request) {
        return ResponseEntity.ok(metadataService.getMetadataFormByCategoryId(request.getCatId()));
    }

    @GetMapping()
    public ResponseEntity<List<ReqAddInfoMetadataTreeDto>> getAllMetadataWithItems() {
        return ResponseEntity.ok(metadataService.getAllMetadataWithItems());
    }

}
