package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.sfa.request.dto.GetHelpRequestsDTO;
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
import java.util.Map;

@Slf4j
public class GetRequestHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);

    private Pageable getPageable(APIGatewayProxyRequestEvent requestEvent) {
        if (requestEvent.getQueryStringParameters() == null) {
            requestEvent.setQueryStringParameters(new HashMap<>());
        }

        int page = Integer.parseInt(
                requestEvent.getQueryStringParameters().getOrDefault("page", "0")
        );

        int size = Integer.parseInt(
                requestEvent.getQueryStringParameters().getOrDefault("size", "10")
        );

        return PageRequest.of(page, size);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context lambdaContext) {
        try {
            Locale locale = getLocaleFromRequest(requestEvent);

            String path = requestEvent.getPath();
            String resource = requestEvent.getResource();

            log.info("Request path: {}", path);
            log.info("Request resource: {}", resource);

            if ((path != null && path.endsWith("/help-requests")) ||
                    (resource != null && resource.endsWith("/help-requests")) ||
                    requestEvent.getPathParameters() == null) {

                Pageable pageable = getPageable(requestEvent);

                SaayamResponse<PagedResponse<GetHelpRequestsDTO>> response =
                        requestService.getAllHelpRequests(pageable, locale);

                return createResponse(HttpStatus.OK.value(), response);
            }

            Map<String, String> pathParameters = requestEvent.getPathParameters();

            String requesterId = pathParameters.get("requesterId");

            if (pathParameters.containsKey("requestId")) {
                String requestId = pathParameters.get("requestId");
                try {
                    SaayamResponse<Request> response = requestService.getRequestById(requesterId, requestId, locale);
                    return createResponse(HttpStatus.OK.value(), response);
                } catch (NotFoundException e) {
                    log.warn("Request not found: {}", e.getMessage());
                    return createErrorResponse(HttpStatus.NOT_FOUND.value(), SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage());
                }
            } else {
                Pageable pageable = getPageable(requestEvent);
                SaayamResponse<PagedResponse<Request>> response = requestService.getRequests(requesterId, pageable, locale);
                return createResponse(HttpStatus.OK.value(), response);
            }
        } catch (Exception e) {
            log.error("Error in GetRequestHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, getLocaleFromRequest(requestEvent));
        }
    }
}