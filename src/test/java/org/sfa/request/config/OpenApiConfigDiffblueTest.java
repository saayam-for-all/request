package org.sfa.request.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OpenApiConfig.class})
@ExtendWith(SpringExtension.class)
class OpenApiConfigDiffblueTest {
    @Autowired
    private OpenApiConfig openApiConfig;

    /**
     * Method under test: {@link OpenApiConfig#customOpenAPI()}
     */
    @Test
    void testCustomOpenAPI() {
        // Arrange and Act
        OpenAPI actualCustomOpenAPIResult = openApiConfig.customOpenAPI();

        // Assert
        Info info = actualCustomOpenAPIResult.getInfo();
        assertEquals("1.0.0", info.getVersion());
        assertEquals("3.0.1", actualCustomOpenAPIResult.getOpenapi());
        assertEquals("API for managing requests in the Saayam system", info.getDescription());
        assertEquals("Saayam Request Handler Service API", info.getTitle());
        assertNull(actualCustomOpenAPIResult.getComponents());
        assertNull(actualCustomOpenAPIResult.getExternalDocs());
        assertNull(actualCustomOpenAPIResult.getPaths());
        assertNull(info.getContact());
        assertNull(info.getLicense());
        assertNull(actualCustomOpenAPIResult.getJsonSchemaDialect());
        assertNull(info.getSummary());
        assertNull(info.getTermsOfService());
        assertNull(actualCustomOpenAPIResult.getSecurity());
        assertNull(actualCustomOpenAPIResult.getServers());
        assertNull(actualCustomOpenAPIResult.getTags());
        assertNull(actualCustomOpenAPIResult.getWebhooks());
        assertNull(actualCustomOpenAPIResult.getExtensions());
        assertNull(info.getExtensions());
        assertEquals(SpecVersion.V30, actualCustomOpenAPIResult.getSpecVersion());
    }
}
