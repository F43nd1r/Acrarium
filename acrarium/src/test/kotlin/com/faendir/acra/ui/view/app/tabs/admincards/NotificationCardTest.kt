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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.mailsettings.MailSettingsRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.github.mvysny.kaributesting.v10._fireValueChange
import com.github.mvysny.kaributesting.v10._get
import com.github.mvysny.kaributesting.v10._value
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Label
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import kotlin.jvm.optionals.getOrNull

class NotificationCardTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val mailSettingsRepository: MailSettingsRepository,
) : UiTest() {
    private val appId = testDataBuilder.createApp()

    override fun setup(): UiParams {
        testDataBuilder.createUser(username = TEST_USER)

        return UiParams(
            route = AdminTab::class,
            routeParameters = AppView.getNavigationParams(appId),
            requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
        )
    }

    @Test
    fun `should be able to set and reset new bug mail setting`() {
        val card = _get<NotificationCard>()
        val checkbox = card._get<Translatable<Label>> { captionId = com.faendir.acra.i18n.Messages.NEW_BUG_MAIL_LABEL }.nextSibling() as Checkbox

        checkbox._value = true
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.newBug).isTrue()

        checkbox._value = false
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.newBug).isFalse()
    }

    @Test
    fun `should be able to set and reset regression mail setting`() {
        val card = _get<NotificationCard>()
        val checkbox = card._get<Translatable<Label>> { captionId = com.faendir.acra.i18n.Messages.REGRESSION_MAIL_LABEL }.nextSibling() as Checkbox

        checkbox._value = true
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.regression).isTrue()

        checkbox._value = false
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.regression).isFalse()
    }

    @Test
    fun `should be able to set and reset spike mail setting`() {
        val card = _get<NotificationCard>()
        val checkbox = card._get<Translatable<Label>> { captionId = com.faendir.acra.i18n.Messages.SPIKE_MAIL_LABEL }.nextSibling() as Checkbox

        checkbox._value = true
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.spike).isTrue()

        checkbox._value = false
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.spike).isFalse()
    }

    @Test
    fun `should be able to set and reset weekly mail setting`() {
        val card = _get<NotificationCard>()
        val checkbox = card._get<Translatable<Label>> { captionId = com.faendir.acra.i18n.Messages.WEEKLY_MAIL_LABEL }.nextSibling() as Checkbox

        checkbox._value = true
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.summary).isTrue()

        checkbox._value = false
        checkbox._fireValueChange()

        expectThat(mailSettingsRepository.find(appId, TEST_USER)?.summary).isFalse()
    }
}

private fun Component.nextSibling(): Component? {
    val parent = this.parent.getOrNull() ?: return null
    val children = parent.children.toList()
    val index = children.indexOf(this)
    return if (index in (0..children.size - 2)) children[index + 1] else null
}