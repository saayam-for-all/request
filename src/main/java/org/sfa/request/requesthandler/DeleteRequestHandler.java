package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.sfa.request.requesthandler.BaseRequestHandler;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Locale;

/**
 * ClassName: DeleteRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/19 16:52
 * @version 1.0
 */
@Slf4j
public class DeleteRequestHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context lambdaContext) {
        try {
            String requesterId = requestEvent.getPathParameters().get("requesterId");
            String requestId = requestEvent.getPathParameters().get("requestId");
            Locale locale = getLocaleFromRequest(requestEvent);

            log.info("Attempting to delete request: {} for requester: {}", requestId, requesterId);
            SaayamResponse<Void> result = requestService.deleteRequest(requesterId, requestId, locale);

            log.info("Delete operation result: {}", result);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(result.getStatusCode())
                    .withBody(objectMapper.writeValueAsString(result));
        } catch (ConflictException e) {
            log.warn("Conflict in DeleteRequestHandler: ", e);
            return createErrorResponse(HttpStatus.CONFLICT.value(), SaayamStatusCode.REQUEST_CONFLICT, e.getMessage());
        } catch (NotFoundException e) {
            log.warn("Request not found in DeleteRequestHandler: ", e);
            return createErrorResponse(HttpStatus.NOT_FOUND.value(), SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error in DeleteRequestHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, getLocaleFromRequest(requestEvent));
        }
    }
}