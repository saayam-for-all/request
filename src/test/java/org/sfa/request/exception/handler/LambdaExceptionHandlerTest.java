package org.sfa.request.exception.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Locale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class LambdaExceptionHandlerTest {
    /**
     * Method under test:
     * {@link LambdaExceptionHandler#handleException(Exception, Context, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: Static initializer failed.
        //   The static initializer of
        //   org.sfa.request.exception.handler.LambdaExceptionHandler
        //   threw org.springframework.beans.factory.BeanCreationException while trying to load it.
        //   Make sure the static initializer of LambdaExceptionHandler
        //   can be executed without throwing exceptions.
        //   Exception: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: Failed to initialize dependency 'dataSourceScriptDatabaseInitializer' of LoadTimeWeaverAware bean 'entityManagerFactory': Error creating bean with name 'dataSourceScriptDatabaseInitializer' defined in class path resource [org/springframework/boot/autoconfigure/sql/init/DataSourceInitializationConfiguration.class]: Unsatisfied dependency expressed through method 'dataSourceScriptDatabaseInitializer' parameter 0: Error creating bean with name 'dataSource' defined in class path resource [org/springframework/boot/autoconfigure/jdbc/DataSourceConfiguration$Hikari.class]: Failed to instantiate [com.zaxxer.hikari.HikariDataSource]: Factory method 'dataSource' threw exception with message: Failed to determine a suitable driver class
        //       at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:326)
        //       at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:205)
        //       at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:952)
        //       at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:624)
        //       at org.springframework.context.annotation.AnnotationConfigApplicationContext.<init>(AnnotationConfigApplicationContext.java:93)
        //       at org.sfa.request.config.SpringContext.getContext(SpringContext.java:23)
        //       at org.sfa.request.exception.handler.LambdaExceptionHandler.<clinit>(LambdaExceptionHandler.java:29)
        //       at java.base/java.lang.Class.forName0(Native Method)
        //       at java.base/java.lang.Class.forName(Class.java:467)

        // Arrange
        // TODO: Populate arranged inputs
        Exception e = null;
        Context context = null;
        Locale locale = null;

        // Act
        APIGatewayProxyResponseEvent actualHandleExceptionResult = LambdaExceptionHandler.handleException(e, context,
                locale);

        // Assert
        // TODO: Add assertions on result
    }
}
