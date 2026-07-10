package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestNoteDTO {
    private Long noteId;
    private String authorUserId;
    private String persona;
    private String content;
    private ZonedDateTime createdAt;
}
