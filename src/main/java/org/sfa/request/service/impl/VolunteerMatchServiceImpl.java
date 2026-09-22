package org.sfa.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.repository.VolunteerMatchRepository;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.sfa.request.service.api.VolunteerMatchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the volunteers to notify about a help request, using the strategy that fits
 * how the help has to be delivered.
 *
 * <ul>
 *   <li><b>IN_PERSON</b> — someone has to physically show up, so proximity decides.
 *       Volunteers are drawn from {@code volunteer_locations} within a configurable
 *       radius of the requester, nearest first.</li>
 *   <li><b>REMOTE</b> — location is irrelevant, so capability decides. Volunteers are
 *       drawn from {@code user_skills} matching the request's help category.</li>
 *   <li><b>UNSPECIFIED / absent</b> — falls back to skill matching. Skills are the
 *       broader and safer signal: a volunteer who lacks the skill cannot help
 *       remotely or in person, whereas proximity alone says nothing about fitness.</li>
 * </ul>
 *
 * <p>Routing keys off {@code requestTypeId} rather than the {@code type} field
 * because that field is a typed enum on one lineage and a String on the other,
 * while the id is an Integer on both.
 *
 * <p>Every matched volunteer gets an IN_APP recipient. Volunteers with an email
 * address on file also get an EMAIL recipient. A volunteer with no email is still
 * reachable in-app rather than being dropped entirely.
 */
@Slf4j
@Service
public class VolunteerMatchServiceImpl implements VolunteerMatchService {

    private final VolunteerMatchRepository volunteerMatchRepository;

    /** Outer radius for in-person proximity matching, in metres. */
    private final double inPersonRadiusMeters;

    /** Cap on volunteers notified for one in-person request (ba#40 D6). */
    private final int inPersonMaxVolunteers;

    public VolunteerMatchServiceImpl(
            VolunteerMatchRepository volunteerMatchRepository,
            @Value("${saayam.matching.in-person.radius-meters:25000}") double inPersonRadiusMeters,
            @Value("${saayam.matching.in-person.max-volunteers:25}") int inPersonMaxVolunteers) {
        this.volunteerMatchRepository = volunteerMatchRepository;
        this.inPersonRadiusMeters = inPersonRadiusMeters;
        this.inPersonMaxVolunteers = inPersonMaxVolunteers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationRecipient> resolveMatchedVolunteerRecipients(Request request) {
        if (isInPerson(request)) {
            return toRecipients(request, matchByProximity(request), "proximity");
        }
        return toRecipients(request, matchBySkill(request), "skill");
    }

    private boolean isInPerson(Request request) {
        if (request.getRequestType() == null) {
            return false;
        }
        Integer typeId = request.getRequestType().getRequestTypeId();
        return typeId != null && typeId == RequestTypeEnum.IN_PERSON.getId();
    }

    /**
     * In-person: volunteers near the requester. The anchor is the requester's own
     * recorded location, since the request itself carries only a free-text locality.
     */
    private List<MatchedVolunteerRow> matchByProximity(Request request) {
        List<MatchedVolunteerRow> matches = volunteerMatchRepository.findVolunteersNearRequester(
                request.getRequesterId(), inPersonRadiusMeters, inPersonMaxVolunteers);

        if (matches.isEmpty()) {
            // Either nobody is nearby or the requester has no location on file. Both
            // are ordinary outcomes, not errors -- the caller records the no-match.
            log.info("No volunteers within {}m of requester for in-person request {}",
                    inPersonRadiusMeters, request.getRequestId());
        }
        return matches;
    }

    /** Remote: volunteers whose registered skills cover the request's category. */
    private List<MatchedVolunteerRow> matchBySkill(Request request) {
        String catId = request.getHelpCategory() != null ? request.getHelpCategory().getCatId() : null;

        if (!StringUtils.hasText(catId)) {
            log.warn("Request {} has no help category; no volunteers can be matched by skill",
                    request.getRequestId());
            return List.of();
        }

        List<MatchedVolunteerRow> matches =
                volunteerMatchRepository.findVolunteersByCategory(catId, request.getRequesterId());

        if (matches.isEmpty()) {
            log.info("No volunteers matched category {} for request {}", catId, request.getRequestId());
        }
        return matches;
    }

    private List<NotificationRecipient> toRecipients(Request request,
                                                     List<MatchedVolunteerRow> matches,
                                                     String strategy) {
        if (matches.isEmpty()) {
            return List.of();
        }

        List<NotificationRecipient> recipients = new ArrayList<>();
        for (MatchedVolunteerRow match : matches) {
            recipients.add(new NotificationRecipient(
                    match.getUserId(), NotificationChannel.IN_APP, null));

            if (StringUtils.hasText(match.getEmailAddress())) {
                recipients.add(new NotificationRecipient(
                        match.getUserId(), NotificationChannel.EMAIL, match.getEmailAddress()));
            } else {
                log.warn("Volunteer {} matched request {} but has no email on file; in-app only",
                        match.getUserId(), request.getRequestId());
            }
        }

        log.info("Matched {} volunteer(s) ({} recipient channel(s)) for request {} by {}",
                matches.size(), recipients.size(), request.getRequestId(), strategy);
        return recipients;
    }
}
