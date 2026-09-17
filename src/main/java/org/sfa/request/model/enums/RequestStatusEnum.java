package org.sfa.request.model.enums;

import lombok.Getter;

@Getter
public enum RequestStatusEnum {
    /** Sentinel used when no lifecycle status has been selected. */
    UNSPECIFIED(0),
    CREATED(1),
    PENDING_VOLUNTEER_ASSIGNMENT(2),
    IN_PROGRESS(3),
    COMPLETED(4),
    CANCELLED(5),
    DELETED(6),

    /**
     * Legacy compatibility value. Ratings are independent records, not request lifecycle states.
     */
    @Deprecated
    RATED_BY_REQUESTER(7),

    /**
     * Legacy compatibility value. Ratings are independent records, not request lifecycle states.
     */
    @Deprecated
    RATED_BY_VOLUNTEER(8),

    /** Volunteer matching exhausted its configured attempts and requires manual handling. */
    VOLUNTEER_NOT_FOUND(9),

    /** The current volunteer assignment must be reconsidered. */
    REASSIGNMENT_REQUESTED(10);

    private final int id;

    RequestStatusEnum(int id) {
        this.id = id;
    }
}
