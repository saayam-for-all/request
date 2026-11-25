package org.sfa.request.requesthandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.sfa.request.constant.SaayamStatusCode;
import org.sfa.request.dto.CommentRequestDTO;
import org.sfa.request.dto.RequestCommentDTO;
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;

import java.util.Locale;

@Slf4j
public class UpdateCommentHandler
        extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService =
            context.getBean(RequestService.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event,
            Context lambdaContext) {

        try {
            if ("OPTIONS".equalsIgnoreCase(event.getHttpMethod())) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(getCorsHeaders())
                        .withBody("");
            }

            Locale locale = getLocaleFromRequest(event);

            CommentRequestDTO body =
                    objectMapper.readValue(
                            event.getBody(),
                            CommentRequestDTO.class
                    );

            if (body.getRequesterId() == null
                    || body.getRequesterId().isBlank()) {

                return createErrorResponse(
                        400,
                        SaayamStatusCode.BAD_REQUEST,
                        "Missing requesterId in request body"
                );
            }

            if (body.getCommentId() == null) {

                return createErrorResponse(
                        400,
                        SaayamStatusCode.BAD_REQUEST,
                        "Missing commentId in request body"
                );
            }

            if (body.getComment() == null
                    || body.getComment().isBlank()) {

                return createErrorResponse(
                        400,
                        SaayamStatusCode.BAD_REQUEST,
                        "Missing comment in request body"
                );
            }

            RequestCommentDTO commentDTO =
                    RequestCommentDTO.builder()
                            .comment(body.getComment())
                            .build();

            RequestCommentDTO response =
                    requestService.updateComment(
                            body.getRequesterId(),
                            body.getCommentId(),
                            commentDTO,
                            locale
                    );

            return createResponse(
                    200,
                    SaayamResponse.success(
                            SaayamStatusCode.SUCCESS,
                            "Comment updated successfully",
                            response
                    )
            );

        } catch (Exception e) {
            log.error("Error in UpdateCommentHandler: ", e);

            return LambdaExceptionHandler.handleException(
                    e,
                    lambdaContext,
                    getLocaleFromRequest(event)
            );
        }
    }
}