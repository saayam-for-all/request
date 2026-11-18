package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Input to steps S1–S3 of Part 3 (issue #95): issue and send the two codes, and
 * register the task token the workflow is paused on.
 *
 * <p>{@code requesterId} and {@code leadVolunteerId} come from step S0's output;
 * {@code taskToken} comes from {@code $$.Task.Token}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "taskToken")
public class OtpIssueRequestDTO {

    private String requestId;

    private String requesterId;

    private String leadVolunteerId;

    private String taskToken;
}
