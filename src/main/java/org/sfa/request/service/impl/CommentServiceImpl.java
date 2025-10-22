package org.sfa.request.service.impl;

import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.CommentDTO;
import org.sfa.request.exception.types.ResourceNotFoundException;
import org.sfa.request.mapper.CommentMapper;
import org.sfa.request.model.entity.Comment;
import org.sfa.request.repository.CommentRepository;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final RequestRepository requestRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, CommentMapper commentMapper, RequestRepository requestRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.requestRepository = requestRepository;
    }

    @Override
    public SaayamResponse<CommentDTO> createComment(String requestId, String authorName, String commentText) {
        boolean requestExistsForComment = requestRepository.existsById(requestId);
        if(!requestExistsForComment){
            throw new ResourceNotFoundException("Request with id:" + requestId + "not found.");
        }

        Comment comment = commentMapper.toEntity(new CommentDTO(authorName, commentText));

        Comment savedCommentForRequest = commentRepository.save(comment);

        CommentDTO commentDTO = commentMapper.toDTO(savedCommentForRequest);

        return SaayamResponse.success(
                SaayamStatusCode.SUCCESS,
                "Comment Created Successfully",
                commentDTO);
    }

    @Override
    public SaayamResponse<CommentDTO> updateComment(String requestId, Long commentId, String commentText) {

        Comment commentToBeUpdated = commentRepository.findByRequestIdAndCommentId(requestId, commentId).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Comment not found with id:" + commentId
                        + "does not belong to request with id:" + requestId));

        commentToBeUpdated.setCommentText(commentText);

        Comment updateComment = commentRepository.save(commentToBeUpdated);

        CommentDTO commentDTO = commentMapper.toDTO(updateComment);

        return SaayamResponse.success(
                SaayamStatusCode.SUCCESS,
                "Comment Content Updated Successfully",
                commentDTO);
    }

    @Override
    public SaayamResponse<CommentDTO> deleteComment(String requestId, Long commentId) {
        Comment commentToBeDeleted = commentRepository.findByRequestIdAndCommentId(requestId, commentId).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Comment not found with id:" + commentId
                                + "does not belong to request with id:" + requestId + "and must already be deleted"));

        commentRepository.delete(commentToBeDeleted);

        return SaayamResponse.success(
                SaayamStatusCode.SUCCESS,
                "Comment Deleted Successfully",
                null);
    }

    @Override
    public SaayamResponse<CommentDTO> getCommentById(String requestId, Long commentId) {
        Comment commentToBeUpdated = commentRepository.findByRequestIdAndCommentId(requestId, commentId).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Comment not found with id:" + commentId
                                + "does not belong to request with id:" + requestId));

        CommentDTO commentDTO = commentMapper.toDTO(commentToBeUpdated);

        return SaayamResponse.success(
                SaayamStatusCode.SUCCESS,
                "Comment Retrieved Successfully",
                commentDTO);
    }
}
