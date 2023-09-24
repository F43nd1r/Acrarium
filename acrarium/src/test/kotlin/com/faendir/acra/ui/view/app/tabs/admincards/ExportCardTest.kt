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
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.DownloadButton
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.github.mvysny.kaributesting.v10._download
import com.github.mvysny.kaributesting.v10._get
import com.github.mvysny.kaributesting.v10._value
import com.vaadin.flow.component.combobox.ComboBox
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.contains

class ExportCardTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
) : UiTest() {
    private val appId = testDataBuilder.createApp()

    override fun setup() = UiParams(
        route = AdminTab::class,
        routeParameters = AppView.getNavigationParams(appId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Test
    fun `should export reports by mail`() {
        val report1 = testDataBuilder.createReport(appId, userMail = "user@example.com")
        val report2 = testDataBuilder.createReport(appId, userMail = "user@example.com")
        val report3 = testDataBuilder.createReport(appId, userMail = "another@example.com")
        val report4 = testDataBuilder.createReport(appId, userMail = null)

        val card = _get<ExportCard>()
        card._get<Translatable<ComboBox<String>>> { captionId = Messages.BY_MAIL }.content._value = "user@example.com"
        val export = card._get<DownloadButton>()._download().toString(Charsets.UTF_8)

        expectThat(export).contains(report1)
        expectThat(export).contains(report2)
        expectThat(export).not().contains(report3)
        expectThat(export).not().contains(report4)
    }

    @Test
    fun `should export reports by id`() {
        val report1 = testDataBuilder.createReport(appId, installationId = "id1")
        val report2 = testDataBuilder.createReport(appId, installationId = "id1")
        val report3 = testDataBuilder.createReport(appId, installationId = "id2")

        val card = _get<ExportCard>()
        card._get<Translatable<ComboBox<String>>> { captionId = Messages.BY_ID }.content._value = "id1"
        val export = card._get<DownloadButton>()._download().toString(Charsets.UTF_8)

        expectThat(export).contains(report1)
        expectThat(export).contains(report2)
        expectThat(export).not().contains(report3)
    }
}