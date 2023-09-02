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

import com.faendir.acra.common.*
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.CustomColumn
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.grid.LocalizedColumn
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.faendir.acra.withAuth
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class CustomColumnCardTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val appRepository: AppRepository,
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
            val customColumnCard = _get<CustomColumnCard>()
            val grid = customColumnCard._get<Grid<*>>()

            grid._expectNone<LocalizedColumn<*>> { rendererIs(ButtonRenderer::class) }
        }
    }

    @Test
    fun `should show edit and delete with EDIT permission`() {
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val customColumnCard = _get<CustomColumnCard>()
            val grid = customColumnCard._get<Grid<*>>()

            grid._expect<LocalizedColumn<*>>(count = 2) { rendererIs(ButtonRenderer::class) }
        }
    }

    @Test
    fun `should be able to add custom column`() {
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val customColumnCard = _get<CustomColumnCard>()
            val grid = customColumnCard._get<Grid<CustomColumn>>()
            customColumnCard._get<Translatable<Button>> { captionId = Messages.ADD_COLUMN }.content.click()
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
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val customColumnCard = _get<CustomColumnCard>()
            val grid = customColumnCard._get<Grid<CustomColumn>>()
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
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            navigateTo(AdminTab::class, AppView.getNavigationParams(appId))
            val customColumnCard = _get<CustomColumnCard>()
            val grid = customColumnCard._get<Grid<CustomColumn>>()
            grid._clickRenderer(0, "delete")

            expectThat(appRepository.getCustomColumns(appId)).isEmpty()
        }
    }
}