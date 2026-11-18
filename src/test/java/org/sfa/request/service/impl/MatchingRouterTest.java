package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.enums.MatchingRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #94: every combination of lead volunteer x calamity x request type ends up on
 * the right matching path.
 */
class MatchingRouterTest {

    /** Builds all 3 x 3 x 3 = 27 combinations, each with the route it must produce. */
    static Stream<Arguments> allCombinations() {
        String[] leadValues = {"YES", "NO", null};
        Boolean[] calamityValues = {true, false, null};
        String[] typeValues = {"IN_PERSON", "REMOTE", null};

        List<Arguments> cases = new ArrayList<>();
        for (String lead : leadValues) {
            for (Boolean calamity : calamityValues) {
                for (String type : typeValues) {
                    cases.add(Arguments.of(lead, calamity, type, expectedRoute(lead, calamity, type)));
                }
            }
        }
        return cases.stream();
    }

    /** The rule from the Part 2 diagram, written out as plainly as possible. */
    private static MatchingRoute expectedRoute(String lead, Boolean calamity, String type) {
        if ("YES".equals(lead)) {
            return MatchingRoute.SELF_LEAD;
        }
        if (!"IN_PERSON".equals(type)) {
            return MatchingRoute.REMOTE;
        }
        if (Boolean.TRUE.equals(calamity)) {
            return MatchingRoute.CALAMITY;
        }
        return MatchingRoute.IN_PERSON;
    }

    @ParameterizedTest(name = "lead={0}, calamity={1}, type={2} -> {3}")
    @MethodSource("allCombinations")
    void routesEveryCombination(String lead, Boolean calamity, String type, MatchingRoute expected) {
        Request request = request(lead, calamity, type);

        assertEquals(expected, MatchingRouter.route(request));
    }

    // ---------- the cases people most often ask about, spelled out ----------

    @Test
    void selfLeadSkipsMatchingEvenForAnInPersonCalamity() {
        assertEquals(MatchingRoute.SELF_LEAD, MatchingRouter.route(request("YES", true, "IN_PERSON")));
    }

    @Test
    void calamityFlagOnARemoteRequestIsIgnored() {
        assertEquals(MatchingRoute.REMOTE, MatchingRouter.route(request("NO", true, "REMOTE")));
    }

    // ---------- names win over numeric ids (issue #78) ----------

    /** The live database has IN_PERSON = 0, but the Java enum says IN_PERSON = 1. */
    @Test
    void requestTypeIsReadByNameNotId() {
        Request request = request("NO", false, null);
        RequestType type = new RequestType();
        type.setRequestTypeId(0);       // live database id for IN_PERSON
        type.setType("IN_PERSON");
        request.setRequestType(type);

        assertEquals(MatchingRoute.IN_PERSON, MatchingRouter.route(request));
    }

    @Test
    void requestTypeNameIsMatchedLoosely() {
        assertTrue(MatchingRouter.isInPerson(request(null, null, "In Person")));
        assertTrue(MatchingRouter.isInPerson(request(null, null, "in-person")));
        assertFalse(MatchingRouter.isInPerson(request(null, null, "Remote")));
    }

    @Test
    void leadVolunteerFallsBackToIdWhenNameIsMissing() {
        Request request = new Request();
        RequestIsLeadVolunteer lead = new RequestIsLeadVolunteer();
        lead.setReqIsleadId(1);
        request.setIsLeadVolunteer(lead);

        assertEquals(MatchingRoute.SELF_LEAD, MatchingRouter.route(request));
    }

    @Test
    void emptyLeadRowIsNotSelfLead() {
        Request request = new Request();
        request.setIsLeadVolunteer(new RequestIsLeadVolunteer());

        assertEquals(MatchingRoute.REMOTE, MatchingRouter.route(request));
    }

    // ---------- requests made for someone else ----------

    @Test
    void requestForOtherIsRecognised() {
        Request request = new Request();
        RequestFor requestFor = new RequestFor();
        requestFor.setRequestFor("OTHER");
        request.setRequestFor(requestFor);

        assertTrue(MatchingRouter.isForSomeoneElse(request));
    }

    @Test
    void requestForSelfOrUnknownIsNotForSomeoneElse() {
        Request self = new Request();
        RequestFor requestFor = new RequestFor();
        requestFor.setRequestFor("SELF");
        self.setRequestFor(requestFor);

        assertFalse(MatchingRouter.isForSomeoneElse(self));
        assertFalse(MatchingRouter.isForSomeoneElse(new Request()));
    }

    private static Request request(String lead, Boolean calamity, String type) {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequesterId("SID-1");
        request.setIsCalamity(calamity);

        if (lead != null) {
            RequestIsLeadVolunteer leadRow = new RequestIsLeadVolunteer();
            leadRow.setReqIslead(lead);
            request.setIsLeadVolunteer(leadRow);
        }
        if (type != null) {
            RequestType typeRow = new RequestType();
            typeRow.setType(type);
            request.setRequestType(typeRow);
        }
        return request;
    }
}
