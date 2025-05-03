package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.CommentDTO;
import org.sfa.request.mapper.CommentMapper;
import org.sfa.request.repository.CommentRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentController commentController;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CommentService commentService() {
            return Mockito.mock(CommentService.class);
        }

        @Bean
        public CommentMapper commentMapper() {
            return Mockito.mock(CommentMapper.class);
        }
    }

    @Test
    void createComment() throws Exception {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setAuthorName("Test User");
        commentDTO.setCommentText("Test comment");
        String requestId = "test123";
        String requesterId = "tester123";

        SaayamResponse<CommentDTO> response =
                SaayamResponse.success(SaayamStatusCode.SUCCESS, " Test Comment Created Successfully", commentDTO);

        Mockito.when(commentService.createComment(eq(requestId), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1.0.0/requests/{requesterId}/{requestId}/comments",
                        requesterId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment Created Successfully"))
                .andExpect(jsonPath("$.data.commentText").value("Test comment"));
    }

    @Test
    void getCommentByRequestId() throws Exception {
        String requestId = "test123";
        String requesterId = "tester123";

        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setCommentId(1L);
        commentDTO.setCommentText("Test comment");

        SaayamResponse<CommentDTO> response =
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "Comment Fetched Successfully", commentDTO);

        Mockito.when(commentService.getCommentById(eq(requestId), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1.0.0/requests/{requesterId}/{requestId}/comments/{commentId}",
                        requesterId, requestId, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment Fetched Successfully"))
                .andExpect(jsonPath("$.data.commentText").value("Test comment"))
                .andExpect(jsonPath("$.data.commentId").value(1));
    }

    @Test
    void updateComment() throws Exception {
        String requestId = "test123";
        String requesterId = "tester123";

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentText("Test comment update");
        commentDTO.setCommentId(1L);

        SaayamResponse<CommentDTO> response =
                SaayamResponse.success(SaayamStatusCode.SUCCESS, "Comment Updated Successfully", commentDTO);

        Mockito.when(commentService.updateComment(eq(requestId), eq(1L), eq("Test comment update")))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1.0.0/requests/{requesterId}/{requestId}/comments/{commentId}",
                        requesterId, requestId, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment Updated Successfully"))
                .andExpect(jsonPath("$.data.commentText").value("Test comment update"));
    }

    @Test
    void deleteComment() throws Exception {
        String requestId = "test123";
        String requesterId = "tester123";
        Long commentId = 1L;

        Mockito.when(commentService.deleteComment(anyString(), anyLong()))
                .thenReturn(SaayamResponse.success(SaayamStatusCode.SUCCESS, "Deleted successfully", new CommentDTO()));

        mockMvc.perform(delete("/api/v1.0.0/requests/{requesterId}/{requestId}/comments/{commentId}",
                        requesterId, requestId, commentId))
                .andExpect(status().isNoContent());
    }
}
