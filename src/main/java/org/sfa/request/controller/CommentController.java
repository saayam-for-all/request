package org.sfa.request.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.CommentDTO;
import org.sfa.request.mapper.CommentMapper;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1.0.0/requests/{requesterId}/{requestId}")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Comment management APIs")
public class CommentController {
    private final CommentMapper commentMapper;
    private final CommentService commentService;


    @GetMapping("/comments/{commentId}")
    public ResponseEntity<SaayamResponse<CommentDTO>> getCommentByRequestId(
            @PathVariable @NotNull String requestId,
            @PathVariable @NotNull Long commentId
    ){
        SaayamResponse<CommentDTO> response = commentService.getCommentById(requestId, commentId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments")
    public ResponseEntity<SaayamResponse<CommentDTO>> createComment(
            @PathVariable @NotNull String requestId,
            @RequestBody @Valid CommentDTO createCommentDTO
    ) {
        SaayamResponse<CommentDTO> response =
                commentService.createComment(
                        requestId, createCommentDTO.getAuthorName(), createCommentDTO.getCommentText());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<SaayamResponse<CommentDTO>> updateComment(
            @PathVariable @NotNull String requestId,
            @PathVariable @NotNull Long commentId,
            @RequestBody @Valid CommentDTO updateCommentDTO
    ) {
        SaayamResponse<CommentDTO> response =
                commentService.updateComment(requestId, commentId, updateCommentDTO.getCommentText());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<SaayamResponse<Void>> deleteComment(
            @PathVariable @NotNull Long commentId,
            @PathVariable @NotNull String requestId
    ) {
        commentService.deleteComment(requestId, commentId);

        return ResponseEntity.noContent().build();
    }
}
