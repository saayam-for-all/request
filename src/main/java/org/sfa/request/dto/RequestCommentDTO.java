package org.sfa.request.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCommentDTO {

    private Long id;

    private String requestId;

    private String comment;

    private String createdBy;

    private LocalDateTime createdAt;
}