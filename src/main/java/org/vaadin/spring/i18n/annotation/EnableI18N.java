package org.vaadin.spring.i18n.annotation;

import org.springframework.context.annotation.Import;
import org.vaadin.spring.i18n.config.I18NConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add this annotation to your application configuration to enable the {@link org.vaadin.spring.i18n.I18N} internationalization support.
 * 
 * @author Gert-Jan Timmer (gjr.timmer@gmail.com)
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(I18NConfiguration.class)
public @interface EnableI18N {

}
