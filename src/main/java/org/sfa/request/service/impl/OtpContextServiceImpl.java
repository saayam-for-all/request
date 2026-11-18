package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.OtpContextDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.VolunteerAssignment;
import org.sfa.request.model.enums.OtpContextIneligibleReason;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;
import org.sfa.request.service.api.OtpContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Step S0 of the Part 3 (OTP verification) workflow, issue #95.
 *
 * <p>Reads the request row and its LEAD row in volunteers_assigned by request ID only
 * (not scoped to a requester, because the caller is the workflow, not a user).</p>
 *
 * <p>The status is compared by <em>name</em>, not numeric ID, because the numeric
 * IDs in RequestStatusEnum are not yet confirmed against the live request_status table.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpContextServiceImpl implements OtpContextService {

    /** Status name that makes a request eligible for OTP generation. */
    static final String ELIGIBLE_STATUS_NAME = RequestStatusEnum.IN_PROGRESS.name();

    private final RequestRepository requestRepository;
    private final VolunteerAssignmentRepository volunteerAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public OtpContextDTO loadContext(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId is required");
        }

        Optional<Request> found = requestRepository.findById(requestId);
        if (found.isEmpty()) {
            return ineligible(requestId, OtpContextIneligibleReason.REQUEST_NOT_FOUND, null, null);
        }

        Request request = found.get();
        String status = statusName(request);
        if (!isEligibleStatus(status)) {
            return ineligible(requestId, OtpContextIneligibleReason.STATUS_NOT_IN_PROGRESS, status, request.getRequesterId());
        }

        List<VolunteerAssignment> leads = volunteerAssignmentRepository.findByRequestId(requestId).stream()
                .filter(a -> VolunteerAssignmentTypeEnum.LEAD.getValue().equals(a.getVolunteerType()))
                .toList();

        if (leads.isEmpty()) {
            return ineligible(requestId, OtpContextIneligibleReason.NO_LEAD_VOLUNTEER, status, request.getRequesterId());
        }
        if (leads.size() > 1) {
            return ineligible(requestId, OtpContextIneligibleReason.MULTIPLE_LEAD_VOLUNTEERS, status, request.getRequesterId());
        }

        log.info("Request {} is eligible for OTP verification", requestId);
        return OtpContextDTO.builder()
                .requestId(requestId)
                .eligible(true)
                .status(status)
                .requesterId(request.getRequesterId())
                .leadVolunteerId(leads.get(0).getVolunteerId())
                .build();
    }

    private static String statusName(Request request) {
        RequestStatus requestStatus = request.getRequestStatus();
        return requestStatus == null ? null : requestStatus.getStatus();
    }

    /**
     * Matches the IN_PROGRESS status name, tolerating case, surrounding whitespace and
     * space/hyphen separators (e.g. "In Progress", "in-progress"), since the exact
     * spelling in the live request_status table is unconfirmed.
     */
    static boolean isEligibleStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
        return ELIGIBLE_STATUS_NAME.equals(normalized);
    }

    private static OtpContextDTO ineligible(String requestId, OtpContextIneligibleReason reason,
                                            String status, String requesterId) {
        log.info("Request {} is not eligible for OTP verification: {} (status={})", requestId, reason, status);
        return OtpContextDTO.builder()
                .requestId(requestId)
                .eligible(false)
                .reason(reason)
                .status(status)
                .requesterId(requesterId)
                .build();
    }
}
