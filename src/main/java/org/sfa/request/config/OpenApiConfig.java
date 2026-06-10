package org.sfa.request.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: OpenApiConfig
 * Package: org.sfa.request.config
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/17 16:12
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Saayam Request Handler Service API")
                        .version("1.0.0")
                        .description("API for managing requests in the Saayam system"));
    }
}
