package org.sfa.request.controller;

import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.EnumsResponse;
import org.sfa.request.service.EnumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/dev/requests/v0.0.1/enums","/enums"})
@RequiredArgsConstructor
public class EnumController {

    private final EnumService enumService;

    @GetMapping
    public ResponseEntity<EnumsResponse> getEnums() {
        return ResponseEntity.ok(enumService.getAllEnums());
    }
}
