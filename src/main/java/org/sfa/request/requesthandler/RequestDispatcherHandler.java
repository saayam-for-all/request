package org.sfa.request.requesthandler;

// AWS Lambda
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

// Services
import org.sfa.request.service.api.RequestService;
import org.sfa.request.service.api.HelpCategoryService;
import org.sfa.request.service.api.MetadataService;

// DTOs and Response
import org.sfa.request.dto.CommentRequestDTO;
import org.sfa.request.dto.RequestCommentDTO;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequesterDTO;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.model.entity.Request;

// Exceptions
import org.sfa.request.exception.handler.LambdaExceptionHandler;
import org.sfa.request.constant.SaayamStatusCode;

// Logging
import lombok.extern.slf4j.Slf4j;

// Java
import java.util.Locale;
import java.util.Map;

@Slf4j
public class RequestDispatcherHandler extends BaseRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final RequestService requestService = context.getBean(RequestService.class);
    private static final HelpCategoryService helpCategoryService = context.getBean(HelpCategoryService.class);
    private static final MetadataService metadataService = context.getBean(MetadataService.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event,
            Context lambdaContext) {

        try {
            String path = event.getPath();
            String method = event.getHttpMethod();

            if (method.equalsIgnoreCase("OPTIONS")) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(getCorsHeaders())
                        .withBody("");
            }

            path = path.replaceFirst("^/dev", "");
            path = path.replaceFirst("^/requests/v0.0.1", "/api");

            log.info(
                    "Incoming request: normalizedPath={}, method={}, rawEvent={}",
                    path,
                    method,
                    event
            );

            Locale locale = getLocaleFromRequest(event);

            // -------------------- COMMENTS --------------------

            if (path.equals("/api/requests/comments")
                    && method.equalsIgnoreCase("POST")) {
                return handleAddComment(event, locale);
            }

            if (path.equals("/api/requests/comments/list")
                    && method.equalsIgnoreCase("POST")) {
                return handleGetComments(event, locale);
            }

            if (path.equals("/api/requests/comments")
                    && method.equalsIgnoreCase("PUT")) {
                return handleUpdateComment(event, locale);
            }

            if (path.equals("/api/requests/comments")
                    && method.equalsIgnoreCase("DELETE")) {
                return handleDeleteComment(event, locale);
            }

            // -------------------- REQUEST --------------------

            if (path.matches("/api/requests/.+")
                    && method.equalsIgnoreCase("POST")) {
                return handleCreateRequest(event, locale);
            }

            // -------------------- HELP CATEGORIES --------------------

            if (path.equals("/api/helpCategories")
                    && method.equalsIgnoreCase("GET")) {
                return createResponse(
                        200,
                        helpCategoryService.getAllHierarchicalCategories()
                );
            }

            if (path.equals("/api/helpCategories/parent")
                    && method.equalsIgnoreCase("POST")) {

                Map<String, Object> body =
                        objectMapper.readValue(event.getBody(), Map.class);

                String parentId = (String) body.get("parentId");

                if (parentId == null || parentId.isBlank()) {
                    return createErrorResponse(
                            400,
                            SaayamStatusCode.BAD_REQUEST,
                            "Missing parentId in request body"
                    );
                }

                return createResponse(
                        200,
                        helpCategoryService.getChildMappingsByParentId(parentId)
                );
            }

            if (path.equals("/api/helpCategories/byId")
                    && method.equalsIgnoreCase("POST")) {

                Map<String, Object> body =
                        objectMapper.readValue(event.getBody(), Map.class);

                String catId = (String) body.get("catId");

                if (catId == null || catId.isBlank()) {
                    return createErrorResponse(
                            400,
                            SaayamStatusCode.BAD_REQUEST,
                            "Missing catId in request body"
                    );
                }

                return createResponse(
                        200,
                        helpCategoryService.getCategoriesByCatId(catId)
                );
            }

            if (path.equals("/api/helpCategories/categoryMap")
                    && method.equalsIgnoreCase("GET")) {
                return createResponse(
                        200,
                        helpCategoryService.getHelpCategoriesTree()
                );
            }

            // -------------------- METADATA --------------------

            if (path.equals("/api/metadata")
                    && method.equalsIgnoreCase("GET")) {
                return createResponse(
                        200,
                        metadataService.getAllMetadataWithItems()
                );
            }

            if (path.equals("/api/metadata/form")
                    && method.equalsIgnoreCase("POST")) {

                Map<String, Object> body =
                        objectMapper.readValue(event.getBody(), Map.class);

                String catId = (String) body.get("catId");

                if (catId == null || catId.isBlank()) {
                    return createErrorResponse(
                            400,
                            SaayamStatusCode.BAD_REQUEST,
                            "Missing catId in request body"
                    );
                }

                return createResponse(
                        200,
                        metadataService.getMetadataFormByCategoryId(catId)
                );
            }

            if (path.matches("/api/metadata/category/.+/fields")
                    && method.equalsIgnoreCase("GET")) {

                String catId = path.split("/")[4];

                return createResponse(
                        200,
                        metadataService.getMetadataByCategoryId(catId)
                );
            }

            return createErrorResponse(
                    404,
                    SaayamStatusCode.BAD_REQUEST,
                    "No handler for path: " + path
            );

        } catch (Exception e) {
            log.error("Unhandled exception: ", e);

            return LambdaExceptionHandler.handleException(
                    e,
                    lambdaContext,
                    Locale.ENGLISH
            );
        }
    }

    // -------------------- COMMENTS METHODS --------------------

    private APIGatewayProxyResponseEvent handleAddComment(
            APIGatewayProxyRequestEvent event,
            Locale locale) throws Exception {

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

        if (body.getRequestId() == null
                || body.getRequestId().isBlank()) {

            return createErrorResponse(
                    400,
                    SaayamStatusCode.BAD_REQUEST,
                    "Missing requestId in request body"
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
                requestService.addComment(
                        body.getRequesterId(),
                        body.getRequestId(),
                        commentDTO,
                        locale
                );

        return createResponse(
                201,
                SaayamResponse.success(
                        SaayamStatusCode.REQUEST_CREATED,
                        "Comment added successfully",
                        response
                )
        );
    }

    private APIGatewayProxyResponseEvent handleGetComments(
            APIGatewayProxyRequestEvent event,
            Locale locale) throws Exception {

        RequesterDTO body =
                objectMapper.readValue(
                        event.getBody(),
                        RequesterDTO.class
                );

        if (body.getRequesterId() == null
                || body.getRequesterId().isBlank()) {

            return createErrorResponse(
                    400,
                    SaayamStatusCode.BAD_REQUEST,
                    "Missing requesterId in request body"
            );
        }

        if (body.getRequestId() == null
                || body.getRequestId().isBlank()) {

            return createErrorResponse(
                    400,
                    SaayamStatusCode.BAD_REQUEST,
                    "Missing requestId in request body"
            );
        }

        return createResponse(
                200,
                SaayamResponse.success(
                        SaayamStatusCode.SUCCESS,
                        "Comments fetched successfully",
                        requestService.getComments(
                                body.getRequesterId(),
                                body.getRequestId(),
                                locale
                        )
                )
        );
    }

    private APIGatewayProxyResponseEvent handleUpdateComment(
            APIGatewayProxyRequestEvent event,
            Locale locale) throws Exception {

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
    }

    private APIGatewayProxyResponseEvent handleDeleteComment(
            APIGatewayProxyRequestEvent event,
            Locale locale) throws Exception {

        RequesterDTO body =
                objectMapper.readValue(
                        event.getBody(),
                        RequesterDTO.class
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

        requestService.deleteComment(
                body.getRequesterId(),
                body.getCommentId(),
                locale
        );

        return createResponse(
                200,
                SaayamResponse.success(
                        SaayamStatusCode.SUCCESS,
                        "Comment deleted successfully",
                        null
                )
        );
    }

    // -------------------- CREATE REQUEST --------------------

    private APIGatewayProxyResponseEvent handleCreateRequest(
            APIGatewayProxyRequestEvent event,
            Locale locale) throws Exception {

        Map<String, String> pathParams =
                event.getPathParameters();

        if (pathParams == null
                || !pathParams.containsKey("requesterId")) {

            return createErrorResponse(
                    400,
                    SaayamStatusCode.BAD_REQUEST,
                    "Missing path parameter: requesterId"
            );
        }

        String requesterId =
                pathParams.get("requesterId");

        RequestDTO requestDTO =
                objectMapper.readValue(
                        event.getBody(),
                        RequestDTO.class
                );

        SaayamResponse<Request> response =
                requestService.createRequest(
                        requesterId,
                        requestDTO,
                        locale
                );

        return createResponse(
                201,
                response
        );
    }
}