package org.sfa.request.config;

import lombok.Getter;
import org.sfa.request.RequestApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * ClassName: SpringContext
 * Package: org.sfa.request.config
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/8 19:37
 * @version 1.0
 */
@Getter
public class SpringContext {
    private static ApplicationContext context;

    public static synchronized ApplicationContext getContext() {
        if (context == null) {
            DatabaseParameterLoader.configureSpringDatasourceIfPresent();
            context = SpringApplication.run(RequestApplication.class);
        }
        return context;
    }
}
