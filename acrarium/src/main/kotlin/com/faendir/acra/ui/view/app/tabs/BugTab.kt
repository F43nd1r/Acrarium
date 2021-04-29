/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QReport
import com.faendir.acra.model.Version
import com.faendir.acra.model.view.VBug
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.BugMerger
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.component.grid.TimeSpanRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.grid
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.tabs.BugTab
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 14.07.18
 */
@View
@Route(value = "bug", layout = AppView::class)
class BugTab(
    private val dataService: DataService,
    private val bugMerger: BugMerger,
    private val localSettings: LocalSettings,
    @ParseAppParameter
    private val app: App
) : AppTab<AcrariumGridView<VBug>>(app) {
    init {
        content {
            val mergeButton = Translatable.createButton(Messages.MERGE_BUGS) {
                val selectedItems: List<VBug> = ArrayList(grid.selectedItems)
                if (selectedItems.size > 1) {
                    val titles = RadioButtonGroup<String>()
                    titles.setItems(selectedItems.map { bug: VBug -> bug.bug.title })
                    titles.value = selectedItems[0].bug.title
                    showFluentDialog {
                        header(Messages.CHOOSE_BUG_GROUP_TITLE)
                        add(titles)
                        createButton {
                            bugMerger.mergeBugs(selectedItems.map { it.bug }, titles.value)
                            grid.deselectAll()
                            grid.dataProvider.refreshAll()
                        }
                    }
                } else {
                    Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
                }
            }.with {
                isEnabled = false
                removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
            header.addComponentAsFirst(mergeButton)
            grid {
                setSelectionMode(Grid.SelectionMode.MULTI)
                asMultiSelect().addSelectionListener { mergeButton.content.isEnabled = it.allSelectedItems.size >= 2 }
                column({ it.reportCount }) {
                    setSortable(QReport.report.count())
                    setCaption(Messages.REPORTS)
                }
                column(TimeSpanRenderer { it.lastReport }) {
                    setSortable(QReport.report.date.max())
                    setCaption(Messages.LATEST_REPORT)
                    sort(GridSortOrder.desc(this).build())
                }
                val versions = dataService.findAllVersions(app)
                val versionCodeNameMap = versions.associate { it.code to it.name }
                column({ versionCodeNameMap[it.highestVersionCode] }) {
                    setSortable(QReport.report.stacktrace.version.code.max())
                    setCaption(Messages.LATEST_VERSION)
                }
                column({ it.userCount }) {
                    setSortable(QReport.report.installationId.countDistinct())
                    setCaption(Messages.AFFECTED_USERS)
                }
                column({ it.bug.title }) {
                    setSortableAndFilterable(QBug.bug.title, Messages.TITLE)
                    setCaption(Messages.TITLE)
                    isAutoWidth = false
                    flexGrow = 1
                }
                column(ComponentRenderer { bug: VBug ->
                    Select(*versions.toTypedArray()).apply {
                        setTextRenderer { it.name }
                        isEmptySelectionAllowed = true
                        emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
                        value = bug.bug.solvedVersion
                        isEnabled = SecurityUtils.hasPermission(app, Permission.Level.EDIT)
                        addValueChangeListener { e: ComponentValueChangeEvent<Select<Version?>?, Version?> -> dataService.setBugSolved(bug.bug, e.value) }
                    }
                }) {
                    setSortable(QBug.bug.solvedVersion)
                    setFilterable(QBug.bug.solvedVersion.isNull, true, Messages.HIDE_SOLVED)
                    setCaption(Messages.SOLVED)
                }
                addOnClickNavigation(ReportTab::class.java) { BugTab.getNavigationParams(it.bug) }
            }
        }
    }

    override fun initContent() = AcrariumGridView(dataService.getBugProvider(app), localSettings::bugGridSettings)
}