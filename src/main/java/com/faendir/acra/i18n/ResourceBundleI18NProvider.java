package com.faendir.acra.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link I18NProvider} that reads messages from {@link ResourceBundle}s with a specific base name.
 *
 * @author lukas
 * @since 10.11.18
 */
public class ResourceBundleI18NProvider implements I18NProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleI18NProvider.class);

    private final String baseName;
    private final String encoding;

    /**
     * Creates a new {@code ResourceBundleI18NProvider} with the given base name and UTF-8 encoding.
     *
     * @param baseName the base name to use, must not be {@code null}.
     */
    public ResourceBundleI18NProvider(String baseName) {
        this(baseName, "UTF-8");
    }

    /**
     * Creates a new {@code ResourceBundleI18NProvider} with the given base name and encoding.
     *
     * @param baseName the base name to use, must not be {@code null}.
     * @param encoding the encoding to use when reading the resource bundle, must not be {@code null}.
     */
    public ResourceBundleI18NProvider(String baseName, String encoding) {
        this.baseName = baseName;
        this.encoding = encoding;
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

    @Override
    public List<Locale> getProvidedLocales() {
        return Stream.of(Locale.getAvailableLocales()).filter(locale -> getResourceBundle(locale) != null).collect(Collectors.toList());
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        final ResourceBundle resourceBundle = getResourceBundle(locale);
        final String message = getString(resourceBundle, key);
        return message == null ? null : String.format(message, params);
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
