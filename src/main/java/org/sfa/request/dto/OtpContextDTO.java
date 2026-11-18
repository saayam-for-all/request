package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sfa.request.model.enums.OtpContextIneligibleReason;

/**
 * Output of step S0 of the Part 3 (OTP verification) workflow, issue #95.
 *
 * <p>When {@code eligible} is true, {@code requesterId} and {@code leadVolunteerId}
 * identify the two parties who will each receive an OTP. When it is false,
 * {@code reason} says why, and the workflow ends without generating codes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpContextDTO {

    private String requestId;

    private boolean eligible;

    /** Set only when {@code eligible} is false. */
    private OtpContextIneligibleReason reason;

    /** The request's current status name as stored in request_status, if the request exists. */
    private String status;

    /** request.req_user_id, if the request exists. */
    private String requesterId;

    /** volunteers_assigned.volunteer_id of the LEAD row, when exactly one exists. */
    private String leadVolunteerId;
}
