/*
 * (C) Copyright 2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.Translatable
import com.github.mvysny.kaributesting.v10._get
import com.github.mvysny.kaributesting.v10._value
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.select.Select
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.*

class SettingsViewTest(
    @Autowired
    @Lazy
    private val localSettings: LocalSettings
) : UiTest() {
    override fun setup() = UiParams(
        route = SettingsView::class,
        requiredAuthorities = setOf(Role.USER)
    )

    @Test
    fun `should be able to set dark theme`() {
        expectThat(localSettings.darkTheme).isFalse()

        _get<Translatable<Checkbox>> { captionId = Messages.DARK_THEME }.content._value = true

        expectThat(localSettings.darkTheme).isTrue()
    }

    @Test
    fun `should be able to set locale`() {
        UI.getCurrent().locale = Locale.ENGLISH

        expectThat(localSettings.locale).isEqualTo(Locale.ENGLISH)

        _get<Translatable<Select<Locale>>> { captionId = Messages.LOCALE }.content._value = Locale.GERMAN

        expectThat(localSettings.locale).isEqualTo(Locale.GERMAN)
    }
}