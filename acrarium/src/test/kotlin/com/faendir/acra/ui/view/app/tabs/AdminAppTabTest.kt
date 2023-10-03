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
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.common.*
import com.faendir.acra.i18n.Messages
import com.faendir.acra.jooq.generated.tables.references.USER
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.CustomColumn
import com.faendir.acra.persistence.fetchValue
import com.faendir.acra.persistence.mailsettings.MailSettingsRepository
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.version.Version
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.ui.component.DownloadButton
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.UploadField
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.component.grid.LocalizedColumn
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.admincards.*
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull

class AdminAppTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
) : UiTest() {
    private val appId = testDataBuilder.createApp()

    override fun setup() = UiParams(
        route = AdminAppTab::class,
        routeParameters = AppView.getNavigationParams(appId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Nested
    inner class CustomColumnAppAdminCardTest(
        @Autowired private val appRepository: AppRepository,
    ) {

        @Test
        fun `should not show edit or delete without ADMIN role`() {
            val card = _get<CustomColumnAppAdminCard>()
            val grid = card._get<Grid<*>>()

            grid._expectNone<LocalizedColumn<*>> { rendererIs(ButtonRenderer::class) }
        }

        @Test
        fun `should show edit and delete with ADMIN permission`() {
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<CustomColumnAppAdminCard>()
                val grid = card._get<Grid<*>>()

                grid._expect<LocalizedColumn<*>>(count = 2) { rendererIs(ButtonRenderer::class) }
            }
        }

        @Test
        fun `should be able to add custom column`() {
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<CustomColumnAppAdminCard>()
                val grid = card._get<Grid<CustomColumn>>()
                card._get<Translatable<Button>> { captionId = Messages.ADD_COLUMN }.content.click()
                expectThat(grid._size()).isEqualTo(1)
                grid.editor._editItem(grid._get(0)) // https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#grid-editors

                grid._getColumnByKey(Messages.PATH).editorComponent<TextField>()._value = "testPath"
                grid._getColumnByKey(Messages.NAME).editorComponent<TextField>()._value = "testName"
                grid._getColumnByKey("edit").editorComponent._get<Translatable<Button>> { captionId = Messages.SAVE }.content.click()

                expectThat(appRepository.getCustomColumns(appId)).containsExactly(CustomColumn(name = "testName", path = "testPath"))
            }
        }

        @Test
        fun `should be able to edit custom column`() {
            val customColumn = testDataBuilder.createCustomColumn(appId)
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<CustomColumnAppAdminCard>()
                val grid = card._get<Grid<CustomColumn>>()
                grid._clickRenderer(0, "edit")
                grid.editor._editItem(grid._get(0)) // https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#grid-editors

                val path = grid._getColumnByKey(Messages.PATH).editorComponent<TextField>()
                expectThat(path._value).isEqualTo(customColumn.path)
                path._value = "testPath"
                val name = grid._getColumnByKey(Messages.NAME).editorComponent<TextField>()
                expectThat(name._value).isEqualTo(customColumn.name)
                name._value = "testName"
                grid._getColumnByKey("edit").editorComponent._get<Translatable<Button>> { captionId = Messages.SAVE }.content.click()

                expectThat(appRepository.getCustomColumns(appId)).containsExactly(CustomColumn(name = "testName", path = "testPath"))
            }
        }

        @Test
        fun `should be able to delete custom column`() {
            testDataBuilder.createCustomColumn(appId)
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<CustomColumnAppAdminCard>()
                val grid = card._get<Grid<CustomColumn>>()
                grid._clickRenderer(0, "delete")

                expectThat(appRepository.getCustomColumns(appId)).isEmpty()
            }
        }
    }

    @Nested
    inner class DangerAppAdminCardTest(
        @Autowired private val appRepository: AppRepository,
        @Autowired private val reportRepository: ReportRepository,
        @Autowired private val jooq: DSLContext,
    ) {
        @Test
        fun `should not show up without admin role`() {
            _expectNone<DangerAppAdminCard>()
        }

        @Test
        fun `should recreate reporter`() {
            val reporter = appRepository.find(appId)!!.reporterUsername
            fun getPassword() = jooq.select(USER.PASSWORD).from(USER).where(USER.USERNAME.eq(reporter)).fetchValue()
            val oldPassword = getPassword()

            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<DangerAppAdminCard>()
                card._get<Translatable<Button>> { captionId = Messages.CREATE }.content._click()
                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

                expectThat(getPassword()).isNotEqualTo(oldPassword)

                _expectOne<Label> {
                    predicates.add { it.element.outerHTML?.contains(reporter) == true }
                }

                _get<Translatable<Button>> { captionId = Messages.CLOSE }.content._click()

                _expectNoDialogs()
            }
        }

        @Test
        fun `should purge old reports by date`() {
            val now = Instant.now()
            val oldReport = testDataBuilder.createReport(appId, date = now.minus(2, ChronoUnit.DAYS))
            val newReport = testDataBuilder.createReport(appId, date = now)

            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<DangerAppAdminCard>()
                card._find<Translatable<Button>> { captionId = Messages.PURGE }.first().content._click()

                _get<Translatable<NumberField>> { captionId = Messages.REPORTS_OLDER_THAN1 }.content._value = 1.0

                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

                expectThat(reportRepository.find(oldReport)).isNull()
                expectThat(reportRepository.find(newReport)).isNotNull()
                _expectNoDialogs()
            }
        }

        @Test
        fun `should purge old reports by version`() {
            val oldReport = testDataBuilder.createReport(appId, version = testDataBuilder.createVersion(appId, code = 1))
            val newReport = testDataBuilder.createReport(appId, version = testDataBuilder.createVersion(appId, code = 2))

            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<DangerAppAdminCard>()
                card._find<Translatable<Button>> { captionId = Messages.PURGE }.last().content._click()

                _get<Translatable<ComboBox<Int>>> { captionId = Messages.REPORTS_BEFORE_VERSION }.content._value = 2

                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

                expectThat(reportRepository.find(oldReport)).isNull()
                expectThat(reportRepository.find(newReport)).isNotNull()
                _expectNoDialogs()
            }
        }

        @Test
        fun `should delete app`() {
            withAuth(Permission(appId, Permission.Level.ADMIN)) {
                val card = _get<DangerAppAdminCard>()
                card._get<Translatable<Button>> { captionId = Messages.DELETE }.content._click()
                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

                expectThat(appRepository.find(appId)).isNull()
                _expectOne<Overview>()
            }
        }
    }

    @Nested
    inner class ExportAppAdminCardTest {
        @Test
        fun `should export reports by mail`() {
            val report1 = testDataBuilder.createReport(appId, userMail = "user@example.com")
            val report2 = testDataBuilder.createReport(appId, userMail = "user@example.com")
            val report3 = testDataBuilder.createReport(appId, userMail = "another@example.com")
            val report4 = testDataBuilder.createReport(appId, userMail = null)

            val card = _get<ExportAppAdminCard>()
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

            val card = _get<ExportAppAdminCard>()
            card._get<Translatable<ComboBox<String>>> { captionId = Messages.BY_ID }.content._value = "id1"
            val export = card._get<DownloadButton>()._download().toString(Charsets.UTF_8)

            expectThat(export).contains(report1)
            expectThat(export).contains(report2)
            expectThat(export).not().contains(report3)
        }
    }

    @Nested
    inner class NotificationAppAdminCardTest(
        @Autowired private val mailSettingsRepository: MailSettingsRepository,
    ) {

        @BeforeEach
        fun setup() {
            testDataBuilder.createUser(username = TEST_USER)
        }

        @Test
        fun `should be able to set and reset new bug mail setting`() {
            val card = _get<NotificationAppAdminCard>()
            val checkbox = card._get<Translatable<Label>> { captionId = Messages.NEW_BUG_MAIL_LABEL }.nextSibling() as Checkbox

            checkbox._value = true
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.newBug).isTrue()

            checkbox._value = false
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.newBug).isFalse()
        }

        @Test
        fun `should be able to set and reset regression mail setting`() {
            val card = _get<NotificationAppAdminCard>()
            val checkbox = card._get<Translatable<Label>> { captionId = Messages.REGRESSION_MAIL_LABEL }.nextSibling() as Checkbox

            checkbox._value = true
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.regression).isTrue()

            checkbox._value = false
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.regression).isFalse()
        }

        @Test
        fun `should be able to set and reset spike mail setting`() {
            val card = _get<NotificationAppAdminCard>()
            val checkbox = card._get<Translatable<Label>> { captionId = Messages.SPIKE_MAIL_LABEL }.nextSibling() as Checkbox

            checkbox._value = true
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.spike).isTrue()

            checkbox._value = false
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.spike).isFalse()
        }

        @Test
        fun `should be able to set and reset weekly mail setting`() {
            val card = _get<NotificationAppAdminCard>()
            val checkbox = card._get<Translatable<Label>> { captionId = Messages.WEEKLY_MAIL_LABEL }.nextSibling() as Checkbox

            checkbox._value = true
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.summary).isTrue()

            checkbox._value = false
            checkbox._fireValueChange()

            expectThat(mailSettingsRepository.find(appId, TEST_USER)?.summary).isFalse()
        }
    }

    @Nested
    inner class VersionAppAdminCardTest(
        @Autowired private val versionRepository: VersionRepository,
    ) {
        @Test
        fun `should not show edit or delete with VIEW permission`() {
            val card = _get<VersionAppAdminCard>()
            val grid = card._get<Grid<*>>()

            grid._expectNone<LocalizedColumn<*>> { rendererIs(ButtonRenderer::class) }
        }

        @Test
        fun `should show edit and delete with EDIT permission`() {
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<VersionAppAdminCard>()
                val grid = card._get<Grid<*>>()

                grid._expect<LocalizedColumn<*>>(count = 2) { rendererIs(ButtonRenderer::class) }
            }
        }

        @Test
        fun `should be able to create version`() {
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<VersionAppAdminCard>()
                card._get<Translatable<Button>> { captionId = Messages.NEW_VERSION }.content.click()

                _get<Translatable<NumberField>> { captionId = Messages.VERSION_CODE }.content._value = 1.0
                _get<Translatable<TextField>> { captionId = Messages.VERSION_FLAVOR }.content._value = "flavor"
                _get<Translatable<TextField>> { captionId = Messages.VERSION_NAME }.content._value = "name"
                _get<Translatable<UploadField>> { captionId = Messages.MAPPING_FILE }.content._get<Upload>()._upload("mappings.txt", file = "mappings".toByteArray())

                _get<Translatable<Button>> { captionId = Messages.CREATE }.content.click()

                expectThat(versionRepository.find(appId, 1, "flavor")).isEqualTo(
                    Version(code = 1, flavor = "flavor", name = "name", appId = appId, mappings = "mappings")
                )
            }
        }

        @Test
        fun `should be able to edit version`() {
            val versionKey = testDataBuilder.createVersion(appId)
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<VersionAppAdminCard>()
                val grid = card._get<Grid<*>>()
                val version = versionRepository.find(appId, versionKey)!!

                grid._clickRenderer(0, "edit")

                val versionCode = _get<Translatable<NumberField>> { captionId = Messages.VERSION_CODE }.content
                expectThat(versionCode._value).isEqualTo(version.code.toDouble())
                versionCode._expectDisabled()
                val versionFlavor = _get<Translatable<TextField>> { captionId = Messages.VERSION_FLAVOR }.content
                expectThat(versionFlavor._value).isEqualTo(version.flavor)
                versionFlavor._expectDisabled()
                val versionName = _get<Translatable<TextField>> { captionId = Messages.VERSION_NAME }.content
                expectThat(versionName._value).isEqualTo(version.name)
                versionName._value = "name"
                val mappings = _get<Translatable<UploadField>> { captionId = Messages.MAPPING_FILE }.content
                expectThat(mappings._value).isNull()
                mappings._get<Upload>()._upload("mappings.txt", file = "mappings".toByteArray())

                _get<VersionEditorDialog>()._get<Translatable<Button>> { captionId = Messages.SAVE }.content.click()

                expectThat(versionRepository.find(appId, versionKey)).isEqualTo(
                    Version(code = versionKey.code, flavor = versionKey.flavor, name = "name", appId = appId, mappings = "mappings")
                )
            }
        }

        @Test
        fun `should be able to delete version`() {
            val versionKey = testDataBuilder.createVersion(appId)
            withAuth(Permission(appId, Permission.Level.EDIT)) {
                val card = _get<VersionAppAdminCard>()
                val grid = card._get<Grid<*>>()

                grid._clickRenderer(0, "delete")
                _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content.click()

                expectThat(versionRepository.find(appId, versionKey)).isNull()
            }
        }
    }
}

private fun Component.nextSibling(): Component? {
    val parent = this.parent.getOrNull() ?: return null
    val children = parent.children.toList()
    val index = children.indexOf(this)
    return if (index in (0..children.size - 2)) children[index + 1] else null
}