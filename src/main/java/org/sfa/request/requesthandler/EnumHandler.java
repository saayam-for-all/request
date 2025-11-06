package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.EnumsResponse;
import org.sfa.request.service.EnumService;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.constant.SaayamStatusCode;

import java.util.Locale;

@Slf4j
public class EnumHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final EnumService enumService = context.getBean(EnumService.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context lambdaContext) {
        try {
            String path = event.getPath();
            String method = event.getHttpMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(getCorsHeaders())
                        .withBody("");
            }

            log.info("Incoming enums request: rawPath={}, method={}, rawEvent={}", path, method, event);

            if ((path.equals("/dev/requests/v0.0.1/enums") || path.equals("/enums"))
                    && method.equalsIgnoreCase("GET")) {
                EnumsResponse response = enumService.getAllEnums();
                return createResponse(200, response);
            } else {
                return createErrorResponse(404, SaayamStatusCode.BAD_REQUEST, "No handler for path: " + path);
            }

        } catch (Exception e) {
            log.error("Exception in EnumHandler: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, Locale.ENGLISH);
        }
    }
}
