/*
 * (C) Copyright 2020-2025 Lukas Morawietz (https://github.com/F43nd1r)
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

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.*

internal class ResourceBundleI18NProviderTest {
    private val provider = ResourceBundleI18NProvider("com.faendir.acra.messages")

    @Test
    fun `getProvidedLocales returns all available locales from resource files`() {
        val locales = provider.getProvidedLocales()
        expectThat(locales).containsExactlyInAnyOrder(Locale.ROOT, Locale.GERMAN, Locale.US)
    }

    @Test
    fun `getTranslation returns correct translation for default locale`() {
        expectThat(provider.getTranslation("hello", Locale.ROOT)).isEqualTo("Hello")
        expectThat(provider.getTranslation("bye", Locale.ROOT)).isEqualTo("Goodbye")
        expectThat(provider.getTranslation("param", Locale.ROOT, "test")).isEqualTo("Value: test")
    }

    @Test
    fun `getTranslation returns correct translation for German locale`() {
        expectThat(provider.getTranslation("hello", Locale.GERMAN)).isEqualTo("Hallo")
        expectThat(provider.getTranslation("bye", Locale.GERMAN)).isEqualTo("Auf Wiedersehen")
        expectThat(provider.getTranslation("param", Locale.GERMAN, "test")).isEqualTo("Wert: test")
    }

    @Test
    fun `getTranslation returns correct translation for US English locale`() {
        expectThat(provider.getTranslation("hello", Locale.US)).isEqualTo("Howdy")
        expectThat(provider.getTranslation("bye", Locale.US)).isEqualTo("See ya")
        expectThat(provider.getTranslation("param", Locale.US, "test")).isEqualTo("Value (US): test")
    }

    @Test
    fun `getTranslation returns null for missing key`() {
        expectThat(provider.getTranslation("nonexistent", Locale.ROOT)).isNull()
    }

    @Test
    fun `getTranslation returns default for missing locale`() {
        expectThat(provider.getTranslation("hello", Locale.FRENCH)).isEqualTo("Hello")
    }
}