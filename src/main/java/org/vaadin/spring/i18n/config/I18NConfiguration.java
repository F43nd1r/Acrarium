package org.vaadin.spring.i18n.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.vaadin.spring.i18n.CompositeMessageSource;
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.MessageProviderCacheCleanupExecutor;

/**
 * Configuration class used by {@literal @}EnableVaadinI18N
 * 
 * Spring configuration for the {@link CompositeMessageSource}. Please remember to
 * define {@link org.vaadin.spring.i18n.MessageProvider} beans that can serve the message source with messages.
 * 
 * @author Gert-Jan Timmer (gjr.timmer@gmail.com)
 * @author Petter Holmström (petter@vaadin.com)
 * @see I18N
 * @see CompositeMessageSource
 */
@Configuration
public class I18NConfiguration {

    @Bean
    I18N i18n(ApplicationContext context) {
        return new I18N(context);
    }

    @Bean
    CompositeMessageSource messageSource(ApplicationContext context) {
        return new CompositeMessageSource(context);
    }

    @Bean
    MessageProviderCacheCleanupExecutor messageProviderCacheCleanupExecutor(Environment environment,
        CompositeMessageSource messageSource) {
        return new MessageProviderCacheCleanupExecutor(environment, messageSource);
    }
}
