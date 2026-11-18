package org.sfa.request.service.api;

import java.util.Map;

/**
 * Resumes a Step Functions execution paused on a {@code .waitForTaskToken} state.
 */
public interface WorkflowCallbackService {

    /**
     * Sends {@code SendTaskSuccess} with the given output.
     *
     * @return true if the execution was resumed; false if the token is no longer
     *         valid (already used, timed out, or its execution has ended)
     */
    boolean sendSuccess(String taskToken, Map<String, Object> output);
}
