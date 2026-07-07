package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.response.SaayamResponse;
import org.springframework.context.ApplicationContext;
import org.sfa.request.config.SpringContext;
import org.sfa.request.config.ObjectMapperConfig;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ClassName: BaseRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * Updated to support CORS headers on all Lambda responses
 *
 * @author Shariq
 * Updated 2025/10/19
 * @version 1.1
 */
@Slf4j
public abstract class BaseRequestHandler<I, O> implements RequestHandler<I, O> {

    protected static final ApplicationContext context = SpringContext.getContext();
    protected static final ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
    protected static final MessageSource messageSource = context.getBean(MessageSource.class);

    protected Locale getLocaleFromRequest(APIGatewayProxyRequestEvent requestEvent) {
        if (requestEvent != null && requestEvent.getHeaders() != null) {
            String languageCode = requestEvent.getHeaders().getOrDefault("Accept-Language", "en-US");
            return Locale.forLanguageTag(languageCode);
        }
        return Locale.getDefault();
    }

    protected Map<String, String> getCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept-Language");
        headers.put("Access-Control-Allow-Credentials", "true");
        return headers;
    }

    protected APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getCorsHeaders()) // 🔥 Set CORS headers
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.error("Error creating response", e);
            String message = messageSource.getMessage(
                    "error.internalServer",
                    new Object[]{e.getMessage()},
                    Locale.getDefault()
            );
            return createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    SaayamStatusCode.INTERNAL_SERVER_ERROR,
                    message
            );
        }
    }

    protected APIGatewayProxyResponseEvent createErrorResponse(int statusCode, SaayamStatusCode saayamCode, String message) {
        SaayamResponse<Void> errorResponse = SaayamResponse.error(statusCode, saayamCode, message);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(getCorsHeaders())
                .withBody(toJson(errorResponse));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"Serialization error\"}";
        }
    }
}
