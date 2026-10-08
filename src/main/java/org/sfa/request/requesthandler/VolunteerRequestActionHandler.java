package org.sfa.request.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.sfa.request.RequestApplication;
import org.sfa.request.dto.VolunteerRequestActionDTO;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Locale;

public class VolunteerRequestActionHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    private static ConfigurableApplicationContext applicationContext;
    private static RequestService requestService;
    private static ObjectMapper objectMapper;

    public VolunteerRequestActionHandler() {

        if (applicationContext == null) {

            applicationContext =
                    SpringApplication.run(
                            RequestApplication.class
                    );

            requestService =
                    applicationContext.getBean(
                            RequestService.class
                    );

            objectMapper =
                    applicationContext.getBean(
                            ObjectMapper.class
                    );
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event,
            Context context) {

        try {

            if (event.getBody() == null
                    || event.getBody().isBlank()) {

                return createResponse(
                        400,
                        "{\"message\":\"Request body is required\"}"
                );
            }

            VolunteerRequestActionDTO actionDTO =
                    objectMapper.readValue(
                            event.getBody(),
                            VolunteerRequestActionDTO.class
                    );

            SaayamResponse<String> response =
                    requestService.processVolunteerAction(
                            actionDTO,
                            Locale.ENGLISH
                    );

            return createResponse(
                    200,
                    objectMapper.writeValueAsString(response)
            );

        } catch (Exception e) {

            context.getLogger().log(
                    "Volunteer action failed: "
                            + e.getMessage()
            );

            return createResponse(
                    500,
                    "{\"message\":\""
                            + escapeJson(e.getMessage())
                            + "\"}"
            );
        }
    }

    private APIGatewayProxyResponseEvent createResponse(
            int statusCode,
            String body) {

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(
                        java.util.Map.of(
                                "Content-Type",
                                "application/json"
                        )
                )
                .withBody(body);
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "Unexpected error";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}