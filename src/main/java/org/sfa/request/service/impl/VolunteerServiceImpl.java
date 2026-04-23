package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.HelperVolunteerDTO;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.ForbiddenException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestVolunteer;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.repository.RequestVolunteerRepository;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.VolunteerService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {

    private final RequestRepository requestRepository;
    private final RequestVolunteerRepository volunteerRepository;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public SaayamResponse<List<HelperVolunteerDTO>> getHelpers(String requesterId, String requestId, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        List<HelperVolunteerDTO> helpers = volunteerRepository.findByRequest(request)
                .stream()
                .map(v -> new HelperVolunteerDTO(v.getVolunteerUserId(), v.getAddedByUserId(), v.getAddedAt()))
                .collect(Collectors.toList());
        String message = messageSource.getMessage("success.helpersRetrieved", new Object[]{requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, helpers);
    }

    @Override
    @Transactional
    public SaayamResponse<HelperVolunteerDTO> addHelper(String requesterId, String requestId, HelperVolunteerDTO dto, Integer actorUserId, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        enforceLeadOnly(actorUserId, request, locale);
        volunteerRepository.findByRequestAndVolunteerUserId(request, dto.getVolunteerUserId())
                .ifPresent(v -> { throw new ConflictException(
                        messageSource.getMessage("error.helperExists", new Object[]{dto.getVolunteerUserId(), requestId}, locale)
                );});
        RequestVolunteer saved = volunteerRepository.save(RequestVolunteer.builder()
                .request(request)
                .volunteerUserId(dto.getVolunteerUserId())
                .addedByUserId(dto.getAddedByUserId())
                .build());
        HelperVolunteerDTO out = new HelperVolunteerDTO(saved.getVolunteerUserId(), saved.getAddedByUserId(), saved.getAddedAt());
        String message = messageSource.getMessage("success.helperAdded", new Object[]{saved.getVolunteerUserId(), requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, out);
    }

    @Override
    @Transactional
    public SaayamResponse<HelperVolunteerDTO> updateHelper(String requesterId, String requestId, String volunteerUserId, HelperVolunteerDTO dto, Integer actorUserId, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        enforceLeadOnly(actorUserId, request, locale);
        RequestVolunteer rv = volunteerRepository.findByRequestAndVolunteerUserId(request, volunteerUserId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.helperNotFound", new Object[]{volunteerUserId, requestId}, locale)
                ));
        // Update allowed fields
        if (dto.getAddedByUserId() != null) {
            rv.setAddedByUserId(dto.getAddedByUserId());
        }
        rv.setUpdatedAt(ZonedDateTime.now());
        RequestVolunteer saved = volunteerRepository.save(rv);
        HelperVolunteerDTO out = new HelperVolunteerDTO(saved.getVolunteerUserId(), saved.getAddedByUserId(), saved.getAddedAt());
        String message = messageSource.getMessage("success.helperAdded", new Object[]{saved.getVolunteerUserId(), requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, out);
    }

    @Override
    @Transactional
    public SaayamResponse<Void> removeHelper(String requesterId, String requestId, String volunteerUserId, Integer actorUserId, Locale locale) {
        Request request = findRequest(requesterId, requestId, locale);
        enforceLeadOnly(actorUserId, request, locale);
        RequestVolunteer rv = volunteerRepository.findByRequestAndVolunteerUserId(request, volunteerUserId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.helperNotFound", new Object[]{volunteerUserId, requestId}, locale)
                ));
        volunteerRepository.delete(rv);
        String message = messageSource.getMessage("success.helperRemoved", new Object[]{volunteerUserId, requestId}, locale);
        return SaayamResponse.success(SaayamStatusCode.SUCCESS, message, null);
    }

    private Request findRequest(String requesterId, String requestId, Locale locale) {
        return requestRepository.findByRequestIdAndRequesterIdIncludingDeleted(requestId, requesterId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.requestNotFound", new Object[]{requestId, requesterId}, locale)
                ));
    }

    private void enforceLeadOnly(Integer actorUserId, Request request, Locale locale) {
        if (actorUserId == null || request.getLeadVolunteerUserId() == null || !actorUserId.equals(request.getLeadVolunteerUserId())) {
            throw new ForbiddenException(messageSource.getMessage("error.forbidden", new Object[]{"Only lead volunteer can perform this action"}, locale));
        }
    }
}
