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
package com.faendir.acra.i18n

import com.vaadin.flow.i18n.I18NProvider
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Implementation of [I18NProvider] that reads messages from [ResourceBundle]s with a specific base name.
 *
 * @author lukas
 * @since 10.11.18
 */
class ResourceBundleI18NProvider(private val baseName: String) : I18NProvider {
    private fun getResourceBundle(locale: Locale, withFallback: Boolean): ResourceBundle? {
        return try {
            ResourceBundle.getBundle(baseName, locale, this.javaClass.classLoader, MessageControl(withFallback))
        } catch (ex: MissingResourceException) {
            if (withFallback) {
                LOGGER.warn("No message bundle with basename [{}] found for locale [{}]", baseName, locale)
            }
            null
        }
    }

    override fun getProvidedLocales() = Locale.getAvailableLocales().filter { getResourceBundle(it, false) != null }

    override fun getTranslation(key: String, locale: Locale, vararg params: Any): String? = getString(getResourceBundle(locale, true), key)?.let { String.format(it, *params) }

    private class MessageControl internal constructor(private val allowFallback: Boolean) : ResourceBundle.Control() {
        override fun getFallbackLocale(baseName: String, locale: Locale): Locale? = if (allowFallback) super.getFallbackLocale(baseName, locale) else null

        override fun getCandidateLocales(baseName: String, locale: Locale): List<Locale> = if (allowFallback) super.getCandidateLocales(baseName, locale) else listOf(locale, Locale.ROOT)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ResourceBundleI18NProvider::class.java)

        private fun getString(bundle: ResourceBundle?, s: String): String? = bundle?.let { try {it.getString(s) } catch (ex: MissingResourceException) { null }}
    }

}