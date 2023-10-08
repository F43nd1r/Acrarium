/*
 * (C) Copyright 2020-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.bug.BugStats
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.BugSolvedVersionSelect
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGridView
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.InstantRenderer
import com.faendir.acra.ui.component.grid.renderer.VersionRenderer
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.BugView
import com.faendir.acra.ui.view.bug.tabs.ReportBugTab
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route

@View
@Route(value = "bug", layout = AppView::class)
class BugAppTab(
    private val bugRepository: BugRepository,
    private val versionRepository: VersionRepository,
    private val localSettings: LocalSettings,
    private val routeParams: RouteParams,
) : Composite<BasicLayoutPersistingFilterableGridView<BugStats, BugStats.Filter, BugStats.Sort>>() {
    override fun initContent(): BasicLayoutPersistingFilterableGridView<BugStats, BugStats.Filter, BugStats.Sort> {
        val appId = routeParams.appId()
        val gridView = BasicLayoutPersistingFilterableGridView(bugRepository.getProvider(appId), localSettings::bugGridSettings) {
            column({ it.reportCount }) {
                setSortable(BugStats.Sort.REPORT_COUNT)
                setCaption(Messages.REPORTS)
            }
            column(InstantRenderer { it.latestReport }) {
                setSortable(BugStats.Sort.LATEST_REPORT)
                setCaption(Messages.LATEST_REPORT)
                sort(GridSortOrder.desc(this).build())
            }
            val versions = versionRepository.getVersionNames(appId)
            column(VersionRenderer(versions) { it.latestVersionKey }) {
                setSortable(BugStats.Sort.LATEST_VERSION_CODE)
                setFilterableIs(versions, { it.name }, { BugStats.Filter.LATEST_VERSION(it.code, it.flavor) }, Messages.APP_VERSION)
                setCaption(Messages.LATEST_VERSION)
            }
            column({ it.affectedInstallations }) {
                setSortable(BugStats.Sort.AFFECTED_INSTALLATIONS)
                setCaption(Messages.AFFECTED_INSTALLATIONS)
            }
            column({ it.title }) {
                setSortable(BugStats.Sort.TITLE)
                setFilterableContains({ BugStats.Filter.TITLE(it) }, Messages.TITLE)
                setCaption(Messages.TITLE)
                isAutoWidth = false
                flexGrow = 1
            }
            column(ComponentRenderer { bug: BugStats -> BugSolvedVersionSelect(appId, bug, versions, bugRepository) }) {
                setSortable(BugStats.Sort.SOLVED_VERSION_CODE)
                setFilterableToggle(BugStats.Filter.IS_NOT_SOLVED_OR_REGRESSION, true, Messages.HIDE_SOLVED)
                setCaption(Messages.SOLVED)
            }
            addOnClickNavigation(ReportBugTab::class.java) { BugView.getNavigationParams(appId, it.id) }
        }
        if (SecurityUtils.hasPermission(appId, Permission.Level.EDIT)) {
            val mergeButton = Translatable.createButton(Messages.MERGE_BUGS) {
                val selectedItems: List<BugStats> = gridView.grid.selectedItems.toList()
                if (selectedItems.size > 1) {
                    val titles = RadioButtonGroup<String>()
                    titles.setItems(selectedItems.map { bug: BugStats -> bug.title })
                    titles.value = selectedItems[0].title
                    showFluentDialog {
                        header(Messages.CHOOSE_BUG_GROUP_TITLE)
                        add(titles)
                        createButton {
                            bugRepository.mergeBugs(appId, selectedItems.map { it.id }, titles.value)
                            gridView.grid.deselectAll()
                            gridView.grid.dataProvider.refreshAll()
                        }
                    }
                } else {
                    Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
                }
            }.with {
                isEnabled = false
                removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
            gridView.grid.setSelectionMode(Grid.SelectionMode.MULTI)
            gridView.grid.asMultiSelect().addSelectionListener { mergeButton.content.isEnabled = it.allSelectedItems.size >= 2 }
            gridView.header.addComponentAsFirst(mergeButton)
        }
        return gridView
    }
}