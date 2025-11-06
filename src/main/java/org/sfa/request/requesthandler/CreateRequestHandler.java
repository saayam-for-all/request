package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.exception.types.EnumUnspecifiedException;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.model.entity.Request;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.http.HttpStatus;

import java.util.Locale;

/**
 * ClassName: CreateRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * @author Shariq
 * Create 2025/11/1 16:51
 * @version 1.0
 */
@Slf4j
public class CreateRequestHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private RequestService requestService;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context lambdaContext) {
        try {

            if (requestService == null) {
                requestService = context.getBean(RequestService.class);
            }

            String method = requestEvent.getHttpMethod();


            if ("OPTIONS".equalsIgnoreCase(method)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(getCorsHeaders())
                        .withBody("");
            }
            log.info("Received create request event: {}", requestEvent);

            String requesterId = (requestEvent.getPathParameters() != null)
                    ? requestEvent.getPathParameters().get("requesterId")
                    : null;

            if (requesterId == null || requesterId.trim().isEmpty()) {
                log.warn("Missing requesterId in path parameters.");
                return createErrorResponse(HttpStatus.BAD_REQUEST.value(), SaayamStatusCode.BAD_REQUEST,
                        "Missing 'requesterId' path parameter");
            }


            RequestDTO requestDTO = objectMapper.readValue(requestEvent.getBody(), RequestDTO.class);
            Locale locale = getLocaleFromRequest(requestEvent);

            log.info("Processing create request for requesterId: {}, requestDTO: {}", requesterId, requestDTO);


            SaayamResponse<Request> response = requestService.createRequest(requesterId, requestDTO, locale);

            log.info("Create request successful. Response: {}", response);
            return createResponse(HttpStatus.CREATED.value(), response);

        } catch (EnumUnspecifiedException e) {
            log.warn("EnumUnspecifiedException in CreateRequestHandler: ", e);
            return createErrorResponse(HttpStatus.BAD_REQUEST.value(), SaayamStatusCode.ENUM_UNSPECIFIED, e.getMessage());

        } catch (InvalidRequestException e) {
            log.warn("InvalidRequestException in CreateRequestHandler: ", e);
            return createErrorResponse(HttpStatus.BAD_REQUEST.value(), SaayamStatusCode.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in CreateRequestHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, getLocaleFromRequest(requestEvent));
        }
    }
}
