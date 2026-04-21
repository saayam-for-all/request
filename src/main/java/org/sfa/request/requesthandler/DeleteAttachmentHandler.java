package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.service.impl.RequestServiceImpl;

import java.util.Locale;
import java.util.Map;

@Slf4j
public class DeleteAttachmentHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final RequestServiceImpl requestService = context.getBean(RequestServiceImpl.class);
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            Map<String, String> body = objectMapper.readValue(event.getBody(), Map.class);
            String requestId = body.get("requestId");
            String requesterId = body.get("requesterId");
            String fileName = body.get("fileName");
            if (requestId == null || requesterId == null || fileName == null) {
                throw new InvalidRequestException("Missing required fields");
            }
            Locale locale = getLocaleFromRequest(event);
            requestService.deleteAttachment(requesterId, requestId, fileName, locale);
            return createResponse(200, Map.of("message", "Attachment deleted successfully"));
        } catch (NotFoundException e) {
            return createErrorResponse(404, SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage());
        } catch (InvalidRequestException e) {
            return createErrorResponse(400, SaayamStatusCode.REQUEST_CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("Delete error", e);
            return LambdaExceptionHandler.handleException(e, ctx, getLocaleFromRequest(event));
        }
    }
}