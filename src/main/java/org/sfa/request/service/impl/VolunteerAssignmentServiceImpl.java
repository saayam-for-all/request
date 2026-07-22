package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.VolunteerAssignmentDTO;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.VolunteerAssignment;
import org.sfa.request.model.enums.RequestStatusEnum;
import org.sfa.request.model.enums.VolunteerAssignmentTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.VolunteerAssignmentRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.NotificationEventService;
import org.sfa.request.service.api.VolunteerAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VolunteerAssignmentServiceImpl implements VolunteerAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(VolunteerAssignmentServiceImpl.class);

    private final VolunteerAssignmentRepository volunteerAssignmentRepository;
    private final RequestRepository requestRepository;
    private final NotificationEventService notificationEventService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public SaayamResponse<Request> assignVolunteer(String requesterId, String requestId, VolunteerAssignmentDTO assignmentDTO, Locale locale) {
        Request request = findActiveRequest(requesterId, requestId, locale);
        String volunteerType = assignmentDTO.getVolunteerType().getValue();

        if (VolunteerAssignmentTypeEnum.LEAD.getValue().equals(volunteerType)) {
            assignLeadVolunteer(request, assignmentDTO.getVolunteerId(), locale);
        } else {
            assignHelpingVolunteer(request, assignmentDTO.getVolunteerId(), locale);
        }

        populateAssignments(request);
        logger.info("Assigned volunteer {} as {} for request {}", assignmentDTO.getVolunteerId(), volunteerType, requestId);
        String message = messageSource.getMessage(
                "success.volunteerAssigned",
                new Object[]{assignmentDTO.getVolunteerId(), volunteerType, requestId},
                locale
        );
        return SaayamResponse.success(SaayamStatusCode.VOLUNTEER_ASSIGNED, message, request);
    }

    @Override
    @Transactional
    public SaayamResponse<Void> removeVolunteer(String requesterId, String requestId, String volunteerId, Locale locale) {
        findActiveRequest(requesterId, requestId, locale);

        VolunteerAssignment assignment = volunteerAssignmentRepository.findByRequestIdAndVolunteerId(requestId, volunteerId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.volunteerAssignmentNotFound", new Object[]{volunteerId, requestId}, locale)
                ));

        volunteerAssignmentRepository.delete(assignment);
        logger.info("Removed volunteer {} from request {}", volunteerId, requestId);
        String message = messageSource.getMessage("success.volunteerRemoved", new Object[]{volunteerId, requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.VOLUNTEER_REMOVED, message, null);
    }

    @Override
    public void populateAssignments(Request request) {
        List<VolunteerAssignment> assignments = volunteerAssignmentRepository.findByRequestId(request.getRequestId());

        assignments.stream()
                .filter(a -> VolunteerAssignmentTypeEnum.LEAD.getValue().equals(a.getVolunteerType()))
                .findFirst()
                .ifPresent(lead -> request.setLeadVolunteerUserId(lead.getVolunteerId()));

        request.setHelpingVolunteerUserIds(
                assignments.stream()
                        .filter(a -> VolunteerAssignmentTypeEnum.HELPING.getValue().equals(a.getVolunteerType()))
                        .map(VolunteerAssignment::getVolunteerId)
                        .toList()
        );
    }

    private void assignLeadVolunteer(Request request, String volunteerId, Locale locale) {
        VolunteerAssignment leadAssignment = volunteerAssignmentRepository
                .findByRequestIdAndVolunteerType(request.getRequestId(), VolunteerAssignmentTypeEnum.LEAD.getValue())
                .orElseGet(() -> VolunteerAssignment.builder()
                        .requestId(request.getRequestId())
                        .volunteerType(VolunteerAssignmentTypeEnum.LEAD.getValue())
                        .build());

        boolean isReassignment = leadAssignment.getVolunteersAssignedId() != null
                && !volunteerId.equals(leadAssignment.getVolunteerId());
        boolean isNewAssignment = leadAssignment.getVolunteersAssignedId() == null;

        leadAssignment.setVolunteerId(volunteerId);
        leadAssignment.setLastUpdateDate(ZonedDateTime.now());
        volunteerAssignmentRepository.save(leadAssignment);

        if (isReassignment || isNewAssignment) {
            notificationEventService.enqueueRequestEvent(NotificationEventType.VOLUNTEER_CHOSEN, request, locale);
        }
    }

    private void assignHelpingVolunteer(Request request, String volunteerId, Locale locale) {
        boolean alreadyAssigned = volunteerAssignmentRepository.existsByRequestIdAndVolunteerIdAndVolunteerType(
                request.getRequestId(), volunteerId, VolunteerAssignmentTypeEnum.HELPING.getValue()
        );

        if (alreadyAssigned) {
            throw new ConflictException(
                    messageSource.getMessage(
                            "error.volunteerAlreadyAssigned",
                            new Object[]{volunteerId, VolunteerAssignmentTypeEnum.HELPING.getValue(), request.getRequestId()},
                            locale
                    )
            );
        }

        VolunteerAssignment helpingAssignment = VolunteerAssignment.builder()
                .requestId(request.getRequestId())
                .volunteerId(volunteerId)
                .volunteerType(VolunteerAssignmentTypeEnum.HELPING.getValue())
                .lastUpdateDate(ZonedDateTime.now())
                .build();

        volunteerAssignmentRepository.save(helpingAssignment);
    }

    private Request findActiveRequest(String requesterId, String requestId, Locale locale) {
        return requestRepository.findActiveByRequestIdAndRequesterId(requestId, requesterId, RequestStatusEnum.DELETED.getId())
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.requestNotFound", new Object[]{requestId, requesterId}, locale)
                ));
    }
}
