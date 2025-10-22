package org.sfa.request.service.api;

import org.sfa.request.dto.CommentDTO;
import org.sfa.request.response.SaayamResponse;

/**
 * ClassName: CommentService
 * Package: org.sfa.request.service.api
 */
public interface CommentService {


    SaayamResponse<CommentDTO> createComment(String requestId, String authorName, String commentText);

    SaayamResponse<CommentDTO> updateComment(String requestId, Long commentId, String commentText);

    SaayamResponse<CommentDTO> deleteComment(String requestId, Long commentId);

    SaayamResponse<CommentDTO> getCommentById(String requestId, Long commentId);
}
