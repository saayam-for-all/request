package org.sfa.request.service.api;

import org.sfa.request.dto.NearestVolunteerDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.model.entity.Request;

import java.util.List;

public interface NotificationLambdaService {
    void notifyNearbyVolunteers(Request savedRequest, RequestDTO requestDTO, List<NearestVolunteerDto> volunteers);
}