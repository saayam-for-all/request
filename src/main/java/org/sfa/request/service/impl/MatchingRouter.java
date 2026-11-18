package org.sfa.request.service.impl;

import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.MatchingRoute;
import org.sfa.request.model.enums.RequestTypeEnum;

import java.util.Locale;

/**
 * Decides which matching path a request takes (issue #94, Part 2 of the workflow):
 *
 * <pre>
 *   creator is the lead volunteer?   yes -> SELF_LEAD (no search)
 *   in-person?                        no -> REMOTE (skill search)
 *   calamity?                        yes -> CALAMITY (2-5 mile ring)
 *                                     no -> IN_PERSON (nearby search)
 * </pre>
 *
 * <p>The calamity flag only counts on in-person requests. The web app only shows the
 * checkbox for in-person requests. If another client sends {@code isCalamity=true} on a
 * remote request, the flag is ignored and the normal skill search runs.
 *
 * <p>Lookup values are compared by <b>name</b> ("YES", "IN_PERSON", "OTHER"), not by
 * numeric id. The ids in the Java enums do not match the live database (issue #78);
 * for example the database has IN_PERSON = 0 and REMOTE = 1. The numeric id is only
 * used when a lookup row has no name, which happens in hand-built test objects.
 *
 * <p>This is a pure function with no database access, so every combination can be
 * unit tested.
 */
public final class MatchingRouter {

    private MatchingRouter() {
        // static helpers only
    }

    public static MatchingRoute route(Request request) {
        if (isSelfLead(request)) {
            return MatchingRoute.SELF_LEAD;
        }
        if (!isInPerson(request)) {
            return MatchingRoute.REMOTE;
        }
        if (Boolean.TRUE.equals(request.getIsCalamity())) {
            return MatchingRoute.CALAMITY;
        }
        return MatchingRoute.IN_PERSON;
    }

    /** True when the creator answered "Yes" to "Lead Volunteer?" on the request form. */
    static boolean isSelfLead(Request request) {
        if (request.getIsLeadVolunteer() == null) {
            return false;
        }
        String name = request.getIsLeadVolunteer().getReqIslead();
        if (name != null) {
            return "YES".equals(normalize(name));
        }
        // No name loaded: 1 means YES in both the code and the live database.
        Integer id = request.getIsLeadVolunteer().getReqIsleadId();
        return id != null && id == 1;
    }

    static boolean isInPerson(Request request) {
        if (request.getRequestType() == null) {
            return false;
        }
        String name = request.getRequestType().getType();
        if (name != null) {
            return "IN_PERSON".equals(normalize(name));
        }
        Integer id = request.getRequestType().getRequestTypeId();
        return id != null && id == RequestTypeEnum.IN_PERSON.getId();
    }

    /**
     * True when the request was made on behalf of someone else. Then the requester's own
     * saved location says nothing about where help is needed, so it must not be used as
     * a fallback search centre.
     */
    static boolean isForSomeoneElse(Request request) {
        if (request.getRequestFor() == null || request.getRequestFor().getRequestFor() == null) {
            return false;
        }
        return "OTHER".equals(normalize(request.getRequestFor().getRequestFor()));
    }

    /** "In Person", "in-person" and "IN_PERSON" all become "IN_PERSON". */
    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
