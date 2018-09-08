package org.vaadin.spring.i18n.support;

import java.io.Serializable;
import java.util.Locale;

/**
 * Interface to be implemented by all components that contain some kind of internationalized content that needs to be
 * updated on the fly when the locale is changed.
 * 
 * @see TranslatableSupport
 * @author Petter Holmström (petter@vaadin.com)
 */
public interface Translatable extends Serializable {

    /**
     * Called when the component should update all of its translatable strings, setting locales, etc. The locale to use
     * 
     * @param locale the new locale to use.
     */
    void updateMessageStrings(Locale locale);
}
