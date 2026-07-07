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
import org.sfa.request.dto.RequestDTO;
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
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context lambdaContext) {
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

            log.info("Incoming request: normalizedPath={}, method={}, rawEvent={}", path, method, event);

            Locale locale = getLocaleFromRequest(event);

            if (path.matches("/api/requests/.+") && method.equalsIgnoreCase("POST")) {
                return handleCreateRequest(event, locale);
            }
            if (path.equals("/api/helpCategories") && method.equalsIgnoreCase("GET")) {
                return createResponse(200, helpCategoryService.getAllHierarchicalCategories());
            }

            if (path.equals("/api/helpCategories/parent") && method.equalsIgnoreCase("POST")) {
                Map<String, Object> body = objectMapper.readValue(event.getBody(), Map.class);
                String parentId = (String) body.get("parentId");
                if (parentId == null || parentId.isBlank()) {
                    return createErrorResponse(400, SaayamStatusCode.BAD_REQUEST, "Missing parentId in request body");
                }
                return createResponse(200, helpCategoryService.getChildMappingsByParentId(parentId));
            }

            if (path.equals("/api/helpCategories/byId") && method.equalsIgnoreCase("POST")) {
                Map<String, Object> body = objectMapper.readValue(event.getBody(), Map.class);
                String catId = (String) body.get("catId");

                if (catId == null || catId.isBlank()) {
                    return createErrorResponse(400, SaayamStatusCode.BAD_REQUEST, "Missing catId in request body");
                }
                return createResponse(200, helpCategoryService.getCategoriesByCatId(catId));
            }

            if (path.equals("/api/helpCategories/categoryMap") && method.equalsIgnoreCase("GET")) {
                return createResponse(200, helpCategoryService.getHelpCategoriesTree());
            }

            // -------------------- METADATA --------------------

            if (path.equals("/api/metadata") && method.equalsIgnoreCase("GET")) {
                return createResponse(200, metadataService.getAllMetadataWithItems());
            }

            if (path.equals("/api/metadata/form") && method.equalsIgnoreCase("POST")) {
                Map<String, Object> body = objectMapper.readValue(event.getBody(), Map.class);
                String catId = (String) body.get("catId");

                if (catId == null || catId.isBlank()) {
                    return createErrorResponse(400, SaayamStatusCode.BAD_REQUEST, "Missing catId in request body");
                }

                return createResponse(200, metadataService.getMetadataFormByCategoryId(catId));
            }

            if (path.matches("/api/metadata/category/.+/fields") && method.equalsIgnoreCase("GET")) {
                String catId = path.split("/")[4];
                return createResponse(200, metadataService.getMetadataByCategoryId(catId));
            }

            return createErrorResponse(404, SaayamStatusCode.BAD_REQUEST, "No handler for path: " + path);

        } catch (Exception e) {
            log.error("Unhandled exception: ", e);
            return LambdaExceptionHandler.handleException(e, lambdaContext, Locale.ENGLISH);
        }
    }

    private APIGatewayProxyResponseEvent handleCreateRequest(APIGatewayProxyRequestEvent event, Locale locale)
            throws Exception {

        Map<String, String> pathParams = event.getPathParameters();

        if (pathParams == null || !pathParams.containsKey("requesterId")) {
            return createErrorResponse(400, SaayamStatusCode.BAD_REQUEST, "Missing path parameter: requesterId");
        }

        String requesterId = pathParams.get("requesterId");

        RequestDTO requestDTO = objectMapper.readValue(event.getBody(), RequestDTO.class);
        SaayamResponse<Request> response = requestService.createRequest(requesterId, requestDTO, locale);

        return createResponse(201, response);
    }
}
