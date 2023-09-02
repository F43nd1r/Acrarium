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

import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.common.navigateTo
import com.faendir.acra.common.rendererIs
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.Version
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.UploadField
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.component.grid.LocalizedColumn
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.faendir.acra.withAuth
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class VersionCardTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val versionRepository: VersionRepository,
) : UiTest() {
    private var appId = AppId(0)

    @BeforeEach
    fun setup() {
        appId = testDataBuilder.createApp()
    }

    @Test
    fun `should not show edit or delete with VIEW permission`() {
        withAuth(Permission(appId, Permission.Level.VIEW)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val versionCard = _get<VersionCard>()
            val grid = versionCard._get<Grid<*>>()

            grid._expectNone<LocalizedColumn<*>> { rendererIs(ButtonRenderer::class) }
        }
    }

    @Test
    fun `should show edit and delete with EDIT permission`() {
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val versionCard = _get<VersionCard>()
            val grid = versionCard._get<Grid<*>>()

            grid._expect<LocalizedColumn<*>>(count = 2) { rendererIs(ButtonRenderer::class) }
        }
    }

    @Test
    fun `should be able to create version`() {
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val versionCard = _get<VersionCard>()
            versionCard._get<Translatable<Button>> { captionId = Messages.NEW_VERSION }.content.click()

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
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val versionCard = _get<VersionCard>()
            val grid = versionCard._get<Grid<*>>()
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
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val versionCard = _get<VersionCard>()
            val grid = versionCard._get<Grid<*>>()

            grid._clickRenderer(0, "delete")
            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content.click()

            expectThat(versionRepository.find(appId, versionKey)).isNull()
        }
    }
}