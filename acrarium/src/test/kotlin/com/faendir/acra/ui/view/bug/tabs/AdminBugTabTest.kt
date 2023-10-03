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
package com.faendir.acra.ui.view.bug.tabs

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.version.toVersionKey
import com.faendir.acra.ui.component.BugSolvedVersionSelect
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.bug.BugView
import com.faendir.acra.ui.view.bug.tabs.admincards.DangerBugAdminCard
import com.faendir.acra.ui.view.bug.tabs.admincards.PropertiesBugAdminCard
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.textfield.TextArea
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo

class AdminBugTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val bugRepository: BugRepository,
) : UiTest() {
    private val appId = testDataBuilder.createApp()
    private val bugId = testDataBuilder.createBug(appId)

    init {
        testDataBuilder.createReport(appId, bugId)
    }

    override fun setup() = UiParams(
        route = AdminBugTab::class, routeParameters = BugView.getNavigationParams(appId, bugId), requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Nested
    inner class PropertiesBugAdminCardTest {
        @Test
        fun `should not show up without edit permission`() {
            _expectNone<PropertiesBugAdminCard>()
        }

        @Test
        fun `should be able to change title`() {
            val newTitle = "newTitle"
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<PropertiesBugAdminCard>()
                card._get<Translatable<TextArea>> { captionId = Messages.TITLE }.content._value = newTitle
                expectThat(bugRepository.find(bugId)?.title).isNotEqualTo(newTitle)

                card._get<Translatable<Button>> { captionId = Messages.SAVE }.content._click()
                expectThat(bugRepository.find(bugId)?.title).isEqualTo(newTitle)
            }
        }

        @Test
        fun `should be able to change solved version`() {
            val newVersion = testDataBuilder.createVersion(appId)
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<PropertiesBugAdminCard>()
                val select = card._get<BugSolvedVersionSelect>()
                select._value = select.listDataView.items.toList().first { it.toVersionKey() == newVersion }

                expectThat(bugRepository.find(bugId)?.solvedVersionKey).isEqualTo(newVersion)
                select._value = null

                expectThat(bugRepository.find(bugId)?.solvedVersionKey).isEqualTo(null)
            }
        }
    }

    @Nested
    inner class DangerBugAdminCardTest {
        @Test
        fun `should not show up without admin permission`() {
            _expectNone<DangerBugAdminCard>()
        }

        @Test
        fun `should be able to delete bug`() {
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                _get<AdminBugTab>()._get<Translatable<Button>> { captionId = Messages.DELETE }.content._click()
                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

                expectThat(bugRepository.find(bugId)).isEqualTo(null)
                _expectOne<Overview>()
            }
        }
    }
}