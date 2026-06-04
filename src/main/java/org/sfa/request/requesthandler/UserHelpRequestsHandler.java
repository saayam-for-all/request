package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.GetHelpRequestsDTO;
import org.sfa.request.dto.UserHelpRequestsDTO;
import org.springframework.data.domain.Sort;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.response.PagedResponse;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Slf4j
public class UserHelpRequestsHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent,
            Context lambdaContext
    ) {
        try {
            Locale locale = getLocaleFromRequest(requestEvent);

            UserHelpRequestsDTO userHelpRequestsDTO =
                    objectMapper.readValue(requestEvent.getBody(), UserHelpRequestsDTO.class);

            int page = userHelpRequestsDTO.getPage() != null ? userHelpRequestsDTO.getPage() : 0;
            int size = userHelpRequestsDTO.getSize() != null ? userHelpRequestsDTO.getSize() : 10;

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "lastUpdatedAt")
            );

            SaayamResponse<PagedResponse<GetHelpRequestsDTO>> response =
                    requestService.getUserHelpRequests(
                            userHelpRequestsDTO.getUserId(),
                            pageable,
                            locale
                    );

            return createResponse(HttpStatus.OK.value(), response);

        } catch (Exception e) {
            log.error("Error in UserHelpRequestsHandler: ", e);
            return LambdaExceptionHandler.handleException(
                    e,
                    lambdaContext,
                    getLocaleFromRequest(requestEvent)
            );
        }
    }
}