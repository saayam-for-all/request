package org.sfa.request.service.api;

import org.sfa.request.dto.NearestVolunteerDto;
import org.sfa.request.dto.UpdateVolunteerLocationResponse;

import java.util.List;

public interface GeoSpatialService {
    UpdateVolunteerLocationResponse updateVolunteerLocation(String userId, String address);
    List<NearestVolunteerDto> findNearestVolunteers(Double latitude, Double longitude);
}