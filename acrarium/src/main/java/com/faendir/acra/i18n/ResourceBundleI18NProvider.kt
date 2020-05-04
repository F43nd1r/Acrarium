/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
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

    /**
     * Creates a new {@code ResourceBundleI18NProvider} with the given base name and UTF-8 encoding.
     *
     * @param baseName the base name to use, must not be {@code null}.
     */
    public ResourceBundleI18NProvider(String baseName) {
        this.baseName = baseName;
    }

    private ResourceBundle getResourceBundle(Locale locale, boolean withFallback) {
        try {
            return ResourceBundle.getBundle(baseName, locale, this.getClass().getClassLoader(), new MessageControl(withFallback));
        } catch (MissingResourceException ex) {
            if(withFallback) {
                LOGGER.warn("No message bundle with basename [{}] found for locale [{}]", baseName, locale);
            }
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
        return Stream.of(Locale.getAvailableLocales()).filter(locale -> getResourceBundle(locale, false) != null).collect(Collectors.toList());
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        final ResourceBundle resourceBundle = getResourceBundle(locale, true);
        final String message = getString(resourceBundle, key);
        return message == null ? null : String.format(message, params);
    }

    private static class MessageControl extends ResourceBundle.Control {
        private final boolean allowFallback;

        private MessageControl(boolean allowFallback) {
            this.allowFallback = allowFallback;
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if(allowFallback) {
                return super.getFallbackLocale(baseName, locale);
            }
            return null;
        }

        @Override
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if(allowFallback) {
                return super.getCandidateLocales(baseName, locale);
            }
            return Arrays.asList(locale, Locale.ROOT);
        }
    }
}
