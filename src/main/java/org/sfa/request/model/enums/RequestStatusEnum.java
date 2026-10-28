package org.sfa.request.model.enums;

import lombok.Getter;

/**
 * Semantic status names used by the request service. The numeric IDs still
 * reflect the request-local seed and are not verified against deployed dev RDS.
 * Resolve persisted statuses by name until Issue #78 approves a canonical map.
 */
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
    RATED_BY_VOLUNTEER(8);

    private final int id;

    RequestStatusEnum(int id) {
        this.id = id;
    }
}

