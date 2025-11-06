package org.sfa.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.EnumsResponse;
import org.sfa.request.model.entity.RequestFor;
import org.sfa.request.model.entity.RequestPriority;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.repository.RequestForRepository;
import org.sfa.request.repository.RequestPriorityRepository;
import org.sfa.request.repository.RequestStatusRepository;
import org.sfa.request.repository.RequestTypeRepository;
import org.sfa.request.service.api.EnumService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnumServiceImpl implements EnumService {

    private final RequestForRepository requestForRepository;
    private final RequestPriorityRepository requestPriorityRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final RequestTypeRepository requestTypeRepository;

    @Override
    public EnumsResponse getAllEnums() {
        Map<Integer, String> requestForMap = requestForRepository.findAll().stream()
                .collect(Collectors.toMap(RequestFor::getRequestForId, RequestFor::getRequestFor));

        Map<Integer, String> requestPriorityMap = requestPriorityRepository.findAll().stream()
                .collect(Collectors.toMap(RequestPriority::getPriorityId, RequestPriority::getPriority));

        Map<Integer, String> requestStatusMap = requestStatusRepository.findAll().stream()
                .collect(Collectors.toMap(RequestStatus::getRequestStatusId, RequestStatus::getStatus));

        Map<Integer, String> requestTypeMap = requestTypeRepository.findAll().stream()
                .collect(Collectors.toMap(RequestType::getRequestTypeId, RequestType::getType));

        return new EnumsResponse(requestForMap, requestPriorityMap, requestStatusMap, requestTypeMap);
    }
}
