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
package com.faendir.acra.settings

import com.faendir.acra.util.tryOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinResponse
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.VaadinSessionScope
import java.io.Serializable
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import javax.servlet.http.Cookie
import kotlin.reflect.KProperty

/**
 * @author lukas
 * @since 10.09.19
 */
@SpringComponent
@VaadinSessionScope
class LocalSettings(private val objectMapper: ObjectMapper) : Serializable {
    private val cache = mutableMapOf<String, String>()

    var darkTheme: Boolean by Cookie({ it?.toBoolean() ?: false }, { it.toString() })

    var locale: Locale by Cookie({
        it?.let { Locale(it) }
            ?: UI.getCurrent()?.locale
            ?: VaadinService.getCurrent().instantiator.i18NProvider.providedLocales?.firstOrNull()
            ?: Locale.getDefault()
    }, { it.toString() })

    var bugGridSettings: GridSettings? by jsonCookie()

    var reportGridSettings: GridSettings? by jsonCookie()

    var installationGridSettings: GridSettings? by jsonCookie()

    private class Cookie<T>(
        private val fromString: LocalSettings.(String?) -> T,
        private val toString: LocalSettings.(T) -> String
    ) {
        operator fun getValue(localSettings: LocalSettings, property: KProperty<*>): T {
            val id = property.name
            return localSettings.fromString(localSettings.cache[id]
                ?: VaadinRequest.getCurrent()?.cookies?.firstOrNull { it.name == id }?.value?.also { localSettings.cache[id] = it })
        }

        operator fun setValue(localSettings: LocalSettings, property: KProperty<*>, value: T) {
            val id = property.name
            val stringValue = localSettings.toString(value)
            VaadinResponse.getCurrent()?.addCookie(Cookie(id, stringValue))
            localSettings.cache[id] = stringValue
        }
    }

    private inline fun <reified T> jsonCookie() = Cookie(
        { tryOrNull { objectMapper.readValue(URLDecoder.decode(it, Charsets.UTF_8), T::class.java) } },
        { URLEncoder.encode(objectMapper.writeValueAsString(it), Charsets.UTF_8) })

}