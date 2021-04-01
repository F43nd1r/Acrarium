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
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.BugMerger
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.util.PARAM
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.TimeSpanRenderer
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

/**
 * @author lukas
 * @since 14.07.18
 */
@View
@Route(value = "bug", layout = AppView::class)
class BugTab(private val dataService: DataService, private val bugMerger: BugMerger, private val localSettings: LocalSettings,
             @Qualifier(PARAM) private val app: App) :
    AppTab<AcrariumGridView<VBug>>(app) {

    init {
        val mergeButton = Translatable.createButton(Messages.MERGE_BUGS) {
            val selectedItems: List<VBug> = ArrayList(content.grid.selectedItems)
            if (selectedItems.size > 1) {
                val titles = RadioButtonGroup<String>()
                titles.setItems(selectedItems.map { bug: VBug -> bug.bug.title })
                titles.value = selectedItems[0].bug.title
                FluentDialog().setTitle(Messages.CHOOSE_BUG_GROUP_TITLE).addComponent(titles).addCreateButton {
                    bugMerger.mergeBugs(selectedItems.map { it.bug }, titles.value)
                    this@BugTab.content.grid.deselectAll()
                    this@BugTab.content.grid.dataProvider.refreshAll()
                }.show()
            } else {
                Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
            }
        }.with {
            isEnabled = false
            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
        }
        content.grid.asMultiSelect().addSelectionListener { mergeButton.content.isEnabled = it.allSelectedItems.size >= 2 }
        content.header.addComponentAsFirst(mergeButton)
    }

    override fun initContent(): AcrariumGridView<VBug> {
        return AcrariumGridView(dataService.getBugProvider(app), localSettings::bugGridSettings) {
            setSelectionMode(Grid.SelectionMode.MULTI)
            addColumn { it.reportCount }.setSortable(QReport.report.count()).setCaption(Messages.REPORTS)
            val dateColumn = addColumn(TimeSpanRenderer { it.lastReport }).setSortable(QReport.report.date.max()).setCaption(Messages.LATEST_REPORT)
            sort(GridSortOrder.desc(dateColumn).build())
            val versions = dataService.findAllVersions(app)
            val versionCodeNameMap = versions.map { it.code to it.name }.toMap()
            addColumn { versionCodeNameMap[it.highestVersionCode] }.setSortable(QReport.report.stacktrace.version.code.max())
                .setCaption(Messages.LATEST_VERSION)
            addColumn { it.userCount }.setSortable(QReport.report.installationId.countDistinct()).setCaption(Messages.AFFECTED_USERS)
            addColumn { it.bug.title }.setSortableAndFilterable(QBug.bug.title, Messages.TITLE).setCaption(Messages.TITLE).setAutoWidth(false).setFlexGrow(1)
            addColumn(ComponentRenderer { bug: VBug ->
                Select(*versions.toTypedArray()).apply {
                    setTextRenderer { it.name }
                    isEmptySelectionAllowed = true
                    emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
                    value = bug.bug.solvedVersion
                    isEnabled = SecurityUtils.hasPermission(app, Permission.Level.EDIT)
                    addValueChangeListener { e: ComponentValueChangeEvent<Select<Version?>?, Version?> -> dataService.setBugSolved(bug.bug, e.value) }
                }
            }).setSortable(QBug.bug.solvedVersion).setCaption(Messages.SOLVED)
                .setFilterable(QBug.bug.solvedVersion.isNull, true, Messages.HIDE_SOLVED)
            addOnClickNavigation(ReportTab::class.java) { it.bug.id }
        }
    }
}