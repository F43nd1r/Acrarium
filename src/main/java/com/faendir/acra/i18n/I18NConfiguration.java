package com.faendir.acra.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lukas
 * @since 10.11.18
 */
@Configuration
public class I18NConfiguration {

    @Bean
    public I18NProvider i18NProvider() {
        return new ResourceBundleI18NProvider("i18n.com.faendir.acra.messages");
    }
}
