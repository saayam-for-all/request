package org.sfa.request.requesthandler;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.sfa.request.requesthandler.BaseRequestHandler;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestUpdateDTO;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Locale;

/**
 * ClassName: UpdateRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/19 16:51
 * @version 1.0
 */

@Slf4j
public class UpdateRequestHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);

    private static final Map<String, String> CORS_HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
            "Access-Control-Allow-Methods", "OPTIONS,PUT"
    );

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context lambdaContext) {
        try {
            if (requestEvent.getBody() == null || requestEvent.getBody().isBlank()) {
                log.error("Request body is missing from API Gateway event. Full event: {}", requestEvent);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.BAD_REQUEST.value())
                        .withBody("{\"success\":false,\"statusCode\":400,\"message\":\"Request body is required\"}");
            }
            RequestUpdateDTO requestUpdateDTO = objectMapper.readValue(requestEvent.getBody(), RequestUpdateDTO.class);
            Locale locale = getLocaleFromRequest(requestEvent);

            log.info("Attempting to update request: {} for requester: {}", requestUpdateDTO.getRequestId(), requestUpdateDTO.getRequesterId());

            SaayamResponse<Request> response = requestService.updateRequest(requestUpdateDTO, locale);

            log.info("Update operation result: {}", response);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.OK.value())
                    .withHeaders(CORS_HEADERS)
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (NotFoundException e) {
            log.warn("Request not found: ", e);
            return createErrorResponse(HttpStatus.NOT_FOUND.value(), SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage()).withHeaders(CORS_HEADERS);
        } catch (Exception e) {
            log.error("Error in UpdateRequestHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, getLocaleFromRequest(requestEvent)).withHeaders(CORS_HEADERS);
        }
    }
}