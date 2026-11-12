package org.sfa.request.model.enums;

import lombok.Getter;

// Keeps dev's existing numeric IDs untouched (unverified against live RDS — see
// issue #78) and only appends new values at the end, matching the additive
// approach taken on the fix/request-status-state-model branch rather than
// renumbering anything that may already be persisted.
@Getter
public enum RequestStatusEnum {
    UNSPECIFIED(0),
    CREATED(1),
    PENDING_VOLUNTEER_ASSIGNMENT(2),
    IN_PROGRESS(3),
    COMPLETED(4),
    CANCELLED(5),
    DELETED(6),
    RATED_BY_REQUESTER(7),
    RATED_BY_VOLUNTEER(8),
    VOLUNTEER_NOT_FOUND(9);

    private final int id;

    RequestStatusEnum(int id) {
        this.id = id;
    }
}

