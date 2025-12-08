package org.sfa.request.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@ContextConfiguration(classes = {InternationalizationConfig.class})
@ExtendWith(SpringExtension.class)
class InternationalizationConfigDiffblueTest {
    @Autowired
    private InternationalizationConfig internationalizationConfig;

    /**
     * Method under test: {@link InternationalizationConfig#messageSource()}
     */
    @Test
    void testMessageSource() {
        // Arrange and Act
        MessageSource actualMessageSourceResult = internationalizationConfig.messageSource();

        // Assert
        assertTrue(actualMessageSourceResult instanceof ReloadableResourceBundleMessageSource);
        assertNull(((ReloadableResourceBundleMessageSource) actualMessageSourceResult).getParentMessageSource());
        Set<String> basenameSet = ((ReloadableResourceBundleMessageSource) actualMessageSourceResult).getBasenameSet();
        assertEquals(1, basenameSet.size());
        assertTrue(basenameSet.contains("classpath:lang/saayam_messages"));
    }

    /**
     * Method under test: {@link InternationalizationConfig#localeResolver()}
     */
    @Test
    void testLocaleResolver() {
        // Arrange and Act
        LocaleResolver actualLocaleResolverResult = internationalizationConfig.localeResolver();

        // Assert
        assertTrue(actualLocaleResolverResult instanceof AcceptHeaderLocaleResolver);
        assertTrue(((AcceptHeaderLocaleResolver) actualLocaleResolverResult).getSupportedLocales().isEmpty());
    }

    /**
     * Method under test: {@link InternationalizationConfig#init()}
     */
    @Test
    void testInit() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Diffblue AI was unable to find a test

        // Arrange and Act
        internationalizationConfig.init();
    }
}
