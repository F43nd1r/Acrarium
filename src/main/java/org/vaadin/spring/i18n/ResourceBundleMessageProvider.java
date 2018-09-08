package org.vaadin.spring.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Implementation of {@link MessageProvider} that reads messages
 * from {@link ResourceBundle}s with a specific base name.
 *
 * @author Petter Holmström (petter@vaadin.com)
 */
public class ResourceBundleMessageProvider implements MessageProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleMessageProvider.class);

    private final String baseName;
    private final String encoding;

    /**
     * Creates a new {@code ResourceBundleMessageProvider} with the given base name and UTF-8 encoding.
     *
     * @param baseName the base name to use, must not be {@code null}.
     */
    public ResourceBundleMessageProvider(String baseName) {
        this(baseName, "UTF-8");
    }

    /**
     * Creates a new {@code ResourceBundleMessageProvider} with the given base name and encoding.
     *
     * @param baseName the base name to use, must not be {@code null}.
     * @param encoding the encoding to use when reading the resource bundle, must not be {@code null}.
     */
    public ResourceBundleMessageProvider(String baseName, String encoding) {
        this.baseName = baseName;
        this.encoding = encoding;
    }

    @Override
    public MessageFormat resolveCode(String s, Locale locale) {
        final ResourceBundle resourceBundle = getResourceBundle(locale);
        final String message = getString(resourceBundle, s);
        return getMessageFormat(message, locale);
    }

    @Override
    public void clearCache() {
        ResourceBundle.clearCache(this.getClass().getClassLoader());
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(baseName, locale, this.getClass().getClassLoader(), new MessageControl());
        } catch (MissingResourceException ex) {
            LOGGER.warn("No message bundle with basename [{}] found for locale [{}]", baseName, locale);
            return null;
        }
    }

    private static String getString(ResourceBundle bundle, String s) {
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getString(s);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    private static MessageFormat getMessageFormat(String message, Locale locale) {
        if (message == null) {
            return null;
        }
        return new MessageFormat(message, locale);
    }

    private class MessageControl extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
            boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            if ("java.properties".equals(format)) {
                final String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
                final InputStream stream = loader.getResourceAsStream(resourceName);
                if (stream == null) {
                    return null; // Not found
                }
                Reader reader = null;
                try {
                    reader = new InputStreamReader(stream, encoding);
                    return new PropertyResourceBundle(reader);
                } catch (UnsupportedEncodingException ex) {
                    stream.close();
                    throw ex;
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            } else {
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        }
    }
}
