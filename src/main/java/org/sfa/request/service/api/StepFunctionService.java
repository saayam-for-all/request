package org.sfa.request.service.api;

import org.sfa.request.dto.StepFunctionInputDTO;

public interface StepFunctionService {

    String startExecution(
            StepFunctionInputDTO input
    );
}