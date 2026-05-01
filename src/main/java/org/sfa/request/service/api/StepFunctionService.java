package org.sfa.request.service.api;

import org.sfa.request.dto.StepFunctionInputDTO;

/**
 * Service interface for AWS Step Functions operations
 * 
 * @author Saayam Team
 * @version 1.0
 */
public interface StepFunctionService {
    
    /**
     * Start a Step Function execution with the given input
     * 
     * @param input The input data for the Step Function
     * @return The execution ARN of the started Step Function
     */
    String startExecution(StepFunctionInputDTO input);
}
