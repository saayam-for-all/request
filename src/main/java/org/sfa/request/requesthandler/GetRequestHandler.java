package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.sfa.request.requesthandler.BaseRequestHandler;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Locale;

/**
 * ClassName: GetRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/19 16:52
 * @version 1.0
 */
@Slf4j
public class GetRequestHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context lambdaContext) {
        try {
            String requesterId = requestEvent.getPathParameters().get("requesterId");
            Locale locale = getLocaleFromRequest(requestEvent);

            if (requestEvent.getPathParameters().containsKey("requestId")) {
                String requestId = requestEvent.getPathParameters().get("requestId");
                try {
                    SaayamResponse<Request> response = requestService.getRequestById(requesterId, requestId, locale);
                    return createResponse(HttpStatus.OK.value(), response);
                } catch (NotFoundException e) {
                    log.warn("Request not found: {}", e.getMessage());
                    return createErrorResponse(HttpStatus.NOT_FOUND.value(), SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage());
                }
            } else {
                if (requestEvent.getQueryStringParameters() == null) {
                    requestEvent.setQueryStringParameters(new HashMap<>());
                }

                int page = Integer.parseInt(requestEvent.getQueryStringParameters().getOrDefault("page", "0"));
                int size = Integer.parseInt(requestEvent.getQueryStringParameters().getOrDefault("size", "10"));
                Pageable pageable = PageRequest.of(page, size);
                SaayamResponse<PagedResponse<Request>> response = requestService.getRequests(requesterId, pageable, locale);
                return createResponse(HttpStatus.OK.value(), response);
            }
        } catch (Exception e) {
            log.error("Error in GetRequestHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, getLocaleFromRequest(requestEvent));
        }
    }
}
