package org.sfa.request.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * ClassName: InternationalizationConfig
 * Package: org.sfa.request.config
 * Description:
 *
 * This class configures internationalization (i18n) settings for the Saayam For All system.
 * It sets up a message source for resolving messages from resource bundles and defines
 * a locale resolver to determine the current locale based on the request's Accept-Language
 * header.
 *
 * Key configurations include:
 * - `messageSource()`: Configures a `ReloadableResourceBundleMessageSource` bean to load
 *   messages from property files located in the `classpath:lang/` directory. It uses UTF-8
 *   encoding and defaults to using the message code as the default message if not found in the
 *   resource bundle.
 * - `localeResolver()`: Configures an `AcceptHeaderLocaleResolver` bean to determine the locale
 *   from the Accept-Language header of HTTP requests. It sets English as the default locale.
 * - `init()`: A method annotated with `@PostConstruct` to set the default locale of the JVM to
 *   English after the bean initialization.
 *
 * This configuration ensures that the application supports multiple languages and can respond
 * appropriately to different user locales.
 *
 * @author Fan Peng
 * Create 2024/7/9 22:48
 * @version 1.0
 */
@Configuration
public class InternationalizationConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:lang/saayam_messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @PostConstruct
    public void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

}