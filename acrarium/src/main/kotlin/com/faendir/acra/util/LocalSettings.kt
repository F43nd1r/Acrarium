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
package com.faendir.acra.util

import com.vaadin.flow.component.UI
import com.vaadin.flow.i18n.I18NProvider
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinResponse
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.VaadinSessionScope
import org.springframework.lang.NonNull
import java.io.Serializable
import java.util.*
import javax.servlet.http.Cookie

/**
 * @author lukas
 * @since 10.09.19
 */
@SpringComponent
@VaadinSessionScope
class LocalSettings : Serializable {
    private val cache = mutableMapOf<String, String>()

    private fun getValue(id: String): String? {
        if (cache.containsKey(id)) {
            return cache[id]
        }
        return VaadinRequest.getCurrent()?.cookies?.firstOrNull { it.name == id }?.run { value }?.also { cache[id] = it }
    }

    private fun setValue(@NonNull id: String, @NonNull value: String) {
        val response = VaadinResponse.getCurrent()
        response?.addCookie(Cookie(id, value))
        cache[id] = value
    }

    var darkTheme: Boolean
        get() = getValue(DARK_THEME)?.let { java.lang.Boolean.parseBoolean(it) } ?: false
        set(darkTheme) = setValue(DARK_THEME, darkTheme.toString())

    var locale: Locale
        get() = getValue(LOCALE)?.let { language: String? -> Locale(language) } ?: currentLocale
        set(locale) = setValue(LOCALE, locale.toString())

    companion object {
        private const val DARK_THEME = "theme"
        private const val LOCALE = "locale"
        val currentLocale: Locale
            get() {
                return UI.getCurrent()?.locale ?: i18NProvider.providedLocales?.firstOrNull() ?: Locale.getDefault()
            }

        val i18NProvider: I18NProvider
            get() = VaadinService.getCurrent().instantiator.i18NProvider
    }
}