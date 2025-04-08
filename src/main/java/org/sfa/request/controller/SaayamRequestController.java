package org.sfa.request.controller;

import org.sfa.request.dto.CreateSaayamRequestDTO;
import org.sfa.request.model.SaayamRequest;
import org.sfa.request.service.SaayamRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests")
public class SaayamRequestController {

    private final SaayamRequestService requestService;

    public SaayamRequestController(SaayamRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<SaayamRequest> createRequest(@Valid @RequestBody CreateSaayamRequestDTO requestDTO) {
        SaayamRequest createdRequest = requestService.createRequest(requestDTO);
        return ResponseEntity.ok(createdRequest);
    }
} 