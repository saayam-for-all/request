package org.sfa.request.model.enums;

/**
 * Which volunteer-matching path a newly created request takes (issue #94, Part 2 of
 * the request workflow). Decided once, by {@code MatchingRouter}, before any search runs.
 */
public enum MatchingRoute {

    /** The creator is the lead volunteer, so no search runs. */
    SELF_LEAD,

    /**
     * In-person calamity request. Volunteers very close to the help location are likely
     * affected by the same calamity, so the search skips an inner zone and looks in a
     * ring around it (by default 2 to 5 miles).
     */
    CALAMITY,

    /** In-person request: volunteers near the location. */
    IN_PERSON,

    /** Remote request: volunteers whose skills match the help category. */
    REMOTE
}
