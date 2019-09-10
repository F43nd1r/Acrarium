/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.lang.NonNull;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author lukas
 * @since 10.09.19
 */
@SpringComponent
@VaadinSessionScope
public class LocalSettings {
    private static final String DARK_THEME = "theme";
    private static final String LOCALE = "locale";

    private final HashMap<String, String> cache = new HashMap<>();

    private Optional<String> getValue(@NonNull String id) {
        if (cache.containsKey(id)) {
            return Optional.ofNullable(cache.get(id));
        }

        VaadinRequest request = VaadinRequest.getCurrent();
        if (request != null) {
            for (Cookie cookie : request.getCookies()) {
                if (id.equals(cookie.getName())) {
                    cache.put(id, cookie.getValue());
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private void setValue(@NonNull String id, @NonNull String value) {
        VaadinResponse response = VaadinResponse.getCurrent();
        if (response != null) {
            response.addCookie(new Cookie(id, value));
        }
        cache.put(id, value);
    }

    public boolean getDarkTheme() {
        return getValue(DARK_THEME).map(Boolean::parseBoolean).orElse(false);
    }

    public void setDarkTheme(boolean darkTheme) {
        setValue(DARK_THEME, String.valueOf(darkTheme));
    }

    public Locale getLocale() {
        return getValue(LOCALE).map(Locale::new).orElse(getCurrentLocale());
    }

    public void setLocale(Locale locale) {
        setValue(LOCALE, locale.toString());
    }


    public static Locale getCurrentLocale() {
        UI ui = UI.getCurrent();
        Locale locale = ui == null ? null : ui.getLocale();
        if (locale == null) {
            List<Locale> locales = getI18NProvider().getProvidedLocales();
            if (locales != null && !locales.isEmpty()) {
                locale = locales.get(0);
            } else {
                locale = Locale.getDefault();
            }
        }
        return locale;
    }

    public static I18NProvider getI18NProvider() {
        return VaadinService.getCurrent().getInstantiator().getI18NProvider();
    }
}
