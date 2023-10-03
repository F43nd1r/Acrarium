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

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.common.rendererIs
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.report.ReportRow
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.grid.LocalizedColumn
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.report.ReportView
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNull

class ReportAppTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val reportRepository: ReportRepository,
) : UiTest() {
    private val appId = testDataBuilder.createApp()

    override fun setup(): UiParams {
        return UiParams(
            route = ReportAppTab::class,
            routeParameters = AppView.getNavigationParams(appId),
            requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
        )
    }

    @Test
    fun `should navigate to report view`() {
        testDataBuilder.createReport(appId)
        reload()

        val grid = _get<Grid<ReportRow>>()
        grid._clickItem(0)

        _expectOne<ReportView>()
    }

    @Test
    fun `should not show delete button without edit permission`() {
        testDataBuilder.createReport(appId)
        reload()

        val grid = _get<Grid<ReportRow>>()

        grid._expectNone<LocalizedColumn<*>> { rendererIs(ButtonRenderer::class) }
    }

    @Test
    fun `should be able to delete report`() {
        val report = testDataBuilder.createReport(appId)

        withAuth(Permission(appId, Permission.Level.EDIT)) {
            val grid = _get<Grid<ReportRow>>()
            grid._clickRenderer(0, Messages.DELETE)

            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

            expectThat(reportRepository.find(report)).isNull()
        }
    }
}