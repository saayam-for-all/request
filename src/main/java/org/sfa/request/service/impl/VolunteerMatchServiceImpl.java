package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.repository.VolunteerMatchRepository;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.sfa.request.service.api.VolunteerMatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches volunteers to a help request on skill category.
 *
 * <p>Every matched volunteer gets an IN_APP recipient. Volunteers with an email
 * address on file also get an EMAIL recipient. A volunteer with no email is still
 * reachable in-app rather than being dropped entirely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerMatchServiceImpl implements VolunteerMatchService {

    private final VolunteerMatchRepository volunteerMatchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationRecipient> resolveMatchedVolunteerRecipients(Request request) {
        String catId = request.getHelpCategory() != null ? request.getHelpCategory().getCatId() : null;

        if (!StringUtils.hasText(catId)) {
            log.warn("Request {} has no help category; no volunteers can be matched", request.getRequestId());
            return List.of();
        }

        List<MatchedVolunteerRow> matches =
                volunteerMatchRepository.findVolunteersByCategory(catId, request.getRequesterId());

        if (matches.isEmpty()) {
            log.info("No volunteers matched category {} for request {}", catId, request.getRequestId());
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

        log.info("Matched {} volunteer(s) ({} recipient channel(s)) for request {} in category {}",
                matches.size(), recipients.size(), request.getRequestId(), catId);
        return recipients;
    }
}
