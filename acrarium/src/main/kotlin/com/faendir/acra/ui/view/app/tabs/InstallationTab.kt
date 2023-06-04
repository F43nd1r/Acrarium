/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.domain.AvatarService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.report.Installation
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.InstallationView
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGridView
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.InstantRenderer
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.installation.tabs.ReportTab
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route

@View
@Route(value = "installation", layout = AppView::class)
class InstallationTab(
    private val reportRepository: ReportRepository,
    private val avatarService: AvatarService,
    private val localSettings: LocalSettings,
    routeParams: RouteParams,
) : Composite<BasicLayoutPersistingFilterableGridView<Installation, Installation.Filter, Installation.Sort>>() {
    private val appId = routeParams.appId()

    override fun initContent() = BasicLayoutPersistingFilterableGridView(reportRepository.getInstallationProvider(appId), localSettings::installationGridSettings) {
        setSelectionMode(Grid.SelectionMode.NONE)
        column(ComponentRenderer { installation -> InstallationView(avatarService).apply { setInstallationId(installation.id) } }) {
            setSortable(Installation.Sort.ID)
            setFilterableContains({ Installation.Filter.ID(it) }, Messages.INSTALLATION)
            setCaption(Messages.INSTALLATION)
            width = "50px"
            isAutoWidth = false
            flexGrow = 1
        }
        column({ it.reportCount }) {
            setSortable(Installation.Sort.REPORT_COUNT)
            setCaption(Messages.REPORTS)
        }
        column(InstantRenderer { it.latestReport }) {
            setSortable(Installation.Sort.LATEST_REPORT)
            setCaption(Messages.LATEST_REPORT)
            sort(GridSortOrder.desc(this).build())
        }
        addOnClickNavigation(ReportTab::class.java) { com.faendir.acra.ui.view.installation.InstallationView.getNavigationParams(appId, it.id) }
    }
}