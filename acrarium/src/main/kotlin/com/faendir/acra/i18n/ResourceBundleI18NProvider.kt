/*
 * (C) Copyright 2018-2025 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.i18n

import com.faendir.acra.util.tryOrNull
import com.vaadin.flow.i18n.I18NProvider
import mu.KotlinLogging
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.util.*

private val logger = KotlinLogging.logger {}

class ResourceBundleI18NProvider(private val baseName: String) : I18NProvider {
    private val locales: List<Locale> by lazy {
        val basePath = baseName.replace('.', '/')
        val pattern = "classpath*:${basePath}*.properties"
        val resolver = PathMatchingResourcePatternResolver(javaClass.classLoader)
        resolver.getResources(pattern).mapNotNull { resource ->
            val filename = resource.filename ?: return@mapNotNull null
            val match = Regex("^${basePath.substringAfterLast('/')}(?<locale>_[a-zA-Z_]+)?\\.properties$").find(filename) ?: return@mapNotNull null
            val suffix = match.groups["locale"]?.value ?: ""
            if (suffix.startsWith("_")) {
                val parts = suffix.drop(1).split('_')
                when (parts.size) {
                    1 -> Locale.Builder().setLanguage(parts[0]).build()
                    2 -> Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
                    3 -> Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).setVariant(parts[2]).build()
                    else -> Locale.ROOT
                }
            } else Locale.ROOT
        }.sortedWith(compareBy({ it.language }, { it.country }, { it.variant }))
    }

    private fun getResourceBundle(locale: Locale): ResourceBundle? {
        return try {
            ResourceBundle.getBundle(baseName, locale, this.javaClass.classLoader)
        } catch (_: MissingResourceException) {
            logger.warn { "No message bundle with basename $baseName found for locale $locale" }
            null
        }
    }

    override fun getProvidedLocales(): List<Locale> = locales

    override fun getTranslation(key: String, locale: Locale, vararg params: Any): String? =
        getResourceBundle(locale)?.tryOrNull { getString(key) }?.let { String.format(it, *params) }


}