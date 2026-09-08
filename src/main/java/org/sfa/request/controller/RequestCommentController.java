package org.sfa.request.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.CommentRequestDTO;
import org.sfa.request.dto.RequestCommentDTO;
import org.sfa.request.dto.RequesterDTO;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1.0.0/requests")
@RequiredArgsConstructor
public class RequestCommentController {

    private final RequestService requestService;
    private final LocaleResolver localeResolver;


    @PostMapping("/{requestId}/comments")
    public ResponseEntity<SaayamResponse<RequestCommentDTO>> addComment(
            @PathVariable String requestId,
            @RequestBody @Valid CommentRequestDTO body,
            HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);

        RequestCommentDTO commentDTO = RequestCommentDTO.builder()
                .comment(body.getComment())
                .build();

        RequestCommentDTO response = requestService.addComment(
                body.getRequesterId(),
                requestId,
                commentDTO,
                locale
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                SaayamResponse.success(
                        SaayamStatusCode.REQUEST_CREATED,
                        "Comment added successfully",
                        response
                )
        );
    }




    @PostMapping("/{requestId}/comments/list")
    public ResponseEntity<SaayamResponse<List<RequestCommentDTO>>> getComments(
            @PathVariable String requestId,
            @RequestBody @Valid RequesterDTO body,
            HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);

        List<RequestCommentDTO> response = requestService.getComments(
                body.getRequesterId(),
                requestId,
                locale
        );

        return ResponseEntity.ok(
                SaayamResponse.success(
                        SaayamStatusCode.SUCCESS,
                        "Comments fetched successfully",
                        response
                )
        );
    }




    @PutMapping("/comments/{id}")
    public ResponseEntity<SaayamResponse<RequestCommentDTO>> updateComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentRequestDTO body,
            HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);

        RequestCommentDTO commentDTO = RequestCommentDTO.builder()
                .comment(body.getComment())
                .build();

        RequestCommentDTO response = requestService.updateComment(
                body.getRequesterId(),
                id,
                commentDTO,
                locale
        );

        return ResponseEntity.ok(
                SaayamResponse.success(
                        SaayamStatusCode.SUCCESS,
                        "Comment updated successfully",
                        response
                )
        );
    }




    @DeleteMapping("/comments/{id}")
    public ResponseEntity<SaayamResponse<Void>> deleteComment(
            @PathVariable Long id,
            @RequestBody @Valid RequesterDTO body,
            HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);

        requestService.deleteComment(
                body.getRequesterId(),
                id,
                locale
        );

        return ResponseEntity.ok(
                SaayamResponse.success(
                        SaayamStatusCode.SUCCESS,
                        "Comment deleted successfully",
                        null
                )
        );
    }

}