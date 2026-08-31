package org.sfa.request.service.api;

import org.sfa.request.dto.RequestDTO;

public interface UserLambdaService {

    String getOrCreateUser(RequestDTO requestDTO);
}