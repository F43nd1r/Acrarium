package org.vaadin.spring.i18n;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * A {@code MessageProvider} provides messages for a {@link CompositeMessageSource}.
 * There can be multiple message provider beans in the same application context.
 *
 * @author Petter Holmström (petter@vaadin.com)
 */
public interface MessageProvider {

    /**
     * Attempts to resolve the specified code for the specified locale.
     *
     * @param s the code of the message, must not be {@code null}.
     * @param locale the locale, must not be {@code null}.
     * @return a {@code MessageFormat} for the message, or {@code null} if not found.
     */
    MessageFormat resolveCode(String s, Locale locale);

    /**
     * Clears any internal caches, forcing the message provider to resolve the codes from the original message source.
     */
    void clearCache();
}
