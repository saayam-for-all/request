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

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class UploadAttachmentsHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestServiceImpl requestService = context.getBean(RequestServiceImpl.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {

        try {
            Map<String, Object> body = objectMapper.readValue(event.getBody(), Map.class);

            String requestId = (String) body.get("requestId");
            String requesterId = (String) body.get("requesterId");
            List<Map<String, String>> files = (List<Map<String, String>>) body.get("files");

            Locale locale = getLocaleFromRequest(event);

            List<String> urls = requestService.uploadBase64Attachments(
                    requesterId,
                    requestId,
                    files,
                    locale
            );

            return createResponse(200, Map.of("fileUrls", urls));

        } catch (NotFoundException e) {
            return createErrorResponse(404, SaayamStatusCode.REQUEST_NOT_FOUND, e.getMessage());
        } catch (InvalidRequestException e) {
            return createErrorResponse(400, SaayamStatusCode.REQUEST_CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("Upload error", e);
            return LambdaExceptionHandler.handleException(e, ctx, getLocaleFromRequest(event));
        }
    }
}