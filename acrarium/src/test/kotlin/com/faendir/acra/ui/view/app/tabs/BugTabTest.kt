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
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.bug.BugStats
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.persistence.version.toVersionKey
import com.faendir.acra.ui.component.BugSolvedVersionSelect
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.BugView
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse

class BugTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val bugRepository: BugRepository,
    @Autowired private val reportRepository: ReportRepository,
    @Autowired private val versionRepository: VersionRepository,
) : UiTest() {
    private val appId = testDataBuilder.createApp()
    override fun setup() = UiParams(
        route = BugTab::class,
        routeParameters = AppView.getNavigationParams(appId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Test
    fun `should not show merge button without EDIT permission`() {
        _expectNone<Translatable<Button>> { captionId = Messages.MERGE_BUGS }
    }

    @Test
    fun `should merge bugs`() {
        val bug1 = testDataBuilder.createBug(appId)
        val report1 = testDataBuilder.createReport(appId, bug = bug1)
        val bug2 = testDataBuilder.createBug(appId)
        val report2 = testDataBuilder.createReport(appId, bug = bug2)
        val bug3 = testDataBuilder.createBug(appId)
        val report3 = testDataBuilder.createReport(appId, bug = bug3)

        withAuth(Permission(appId, Permission.Level.EDIT)) {
            val tab = _get<BugTab>()
            val grid = _get<Grid<BugStats>>()

            val items = grid._findAll()
            grid.selectionModel.selectFromClient(items.first { it.id == bug1 })
            grid.selectionModel.selectFromClient(items.first { it.id == bug2 })
            tab._get<Translatable<Button>> { captionId = Messages.MERGE_BUGS }.content._click()
            _get<Translatable<Button>> { captionId = Messages.CREATE }.content._click()

            expectThat(bugRepository.getProvider(appId).size(emptySet())).isEqualTo(2)
            expectThat(reportRepository.find(report1)!!.bugId).isEqualTo(bug1)
            expectThat(reportRepository.find(report2)!!.bugId).isEqualTo(bug1)
            expectThat(reportRepository.find(report3)!!.bugId).isEqualTo(bug3)
        }
    }

    @Test
    fun `should not allow to solve a bug without EDIT permission`() {
        val bug = testDataBuilder.createBug(appId)
        testDataBuilder.createReport(appId, bug = bug)
        reload()

        val grid = _get<Grid<BugStats>>()
        val versionSelect = grid._getCellComponent(0, Messages.SOLVED) as BugSolvedVersionSelect
        expectThat(versionSelect.isEnabled).isFalse()
    }

    @Test
    fun `should solve bug`() {
        val version = testDataBuilder.createVersion(appId)
        val bug = testDataBuilder.createBug(appId)
        testDataBuilder.createReport(appId, bug = bug, version = version)

        withAuth(Permission(appId, Permission.Level.EDIT)) {
            val grid = _get<Grid<BugStats>>()
            val versionSelect = grid._getCellComponent(0, Messages.SOLVED) as BugSolvedVersionSelect
            versionSelect._value = versionRepository.getVersionNames(appId).first { it.toVersionKey() == version }

            expectThat(bugRepository.find(bug)!!.solvedVersionKey).isEqualTo(version)
        }
    }

    @Test
    fun `should navigate to bug view`() {
        val bug = testDataBuilder.createBug(appId)
        testDataBuilder.createReport(appId, bug = bug)
        reload()

        val grid = _get<Grid<BugStats>>()
        grid._clickItem(0)

        _expectOne<BugView>()
    }
}