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

import java.util.Locale;

/**
 * ClassName: BaseRequestHandler
 * Package: org.sfa.request.requesthandler
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/8 19:42
 * @version 1.0
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

    protected APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.error("Error creating response", e);
            String message = messageSource.getMessage("error.internalServer", new Object[]{e.getMessage()}, Locale.getDefault());
            return createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    SaayamStatusCode.INTERNAL_SERVER_ERROR,
                    message
            );
        }
    }

    protected APIGatewayProxyResponseEvent createErrorResponse(int statusCode, SaayamStatusCode saayamCode, String message) {
        SaayamResponse<Void> errorResponse = SaayamResponse.error(statusCode, saayamCode, message);
        return createResponse(statusCode, errorResponse);
    }
}