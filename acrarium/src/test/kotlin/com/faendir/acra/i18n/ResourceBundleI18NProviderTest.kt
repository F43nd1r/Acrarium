/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.*

internal class ResourceBundleI18NProviderTest {

    private lateinit var bundle: ResourceBundle

    private lateinit var provider: ResourceBundleI18NProvider

    private val test = "TEST"
    private val result = "Hello"

    @BeforeEach
    fun setUp() {
        provider = ResourceBundleI18NProvider(test)
        mockkStatic(ResourceBundle::class)
        bundle = mockk()
        every { ResourceBundle.getBundle(any(), any(), any(), any()) } returns null
        every { ResourceBundle.getBundle(any(), Locale.US, any(), any()) } returns bundle
        every { bundle.getString(test) } returns result
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(ResourceBundle::class)
    }

    @Test
    fun getProvidedLocales() {
        expectThat(provider.providedLocales).isEqualTo(listOf(Locale.US))
    }

    @Test
    fun getTranslation() {
        expectThat(provider.getTranslation(test, Locale.US)).isEqualTo(result)
        expectThat(provider.getTranslation(result, Locale.US)).isNull()
        expectThat(provider.getTranslation(test, Locale.FRANCE)).isNull()
    }
}