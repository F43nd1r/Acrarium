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

import com.faendir.acra.dataprovider.QueryDslFilterWithParameter
import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QReport
import com.faendir.acra.model.Version
import com.faendir.acra.model.view.VBug
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.BugMerger
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.TimeSpanRenderer
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import java.util.*

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "bug", layout = AppView::class)
class BugTab constructor(dataService: DataService, private val bugMerger: BugMerger) : AppTab<VerticalLayout>(dataService) {

    init {
        content.alignItems = FlexComponent.Alignment.START
        content.setSizeFull()
    }

    override fun init(app: App) {
        val bugs = AcrariumGrid(dataService.getBugProvider(app))
        bugs.setSelectionMode(Grid.SelectionMode.MULTI)
        val countColumn = bugs.addColumn { it.reportCount }.setSortable(QReport.report.count()).setCaption(Messages.REPORTS)
        val dateColumn = bugs.addColumn(TimeSpanRenderer { it.lastReport }).setSortable(QReport.report.date.max()).setCaption(Messages.LATEST_REPORT)
        bugs.sort(GridSortOrder.desc(dateColumn).build())
        val versions = dataService.findAllVersions(app)
        val versionCodeNameMap = versions.map { it.code to it.name }.toMap()
        bugs.addColumn { versionCodeNameMap[it.highestVersionCode] }.setSortable(QReport.report.stacktrace.version.code.max()).setCaption(Messages.LATEST_VERSION)
        bugs.addColumn { it.userCount }.setSortable(QReport.report.installationId.countDistinct()).setCaption(Messages.AFFECTED_USERS)
        bugs.addColumn { it.bug.title }.setSortableAndFilterable(QBug.bug.title).setCaption(Messages.TITLE).setAutoWidth(false).flexGrow = 1
        bugs.addColumn(ComponentRenderer { bug: VBug ->
            Select(*versions.toTypedArray()).apply {
                setTextRenderer { it.name }
                isEmptySelectionAllowed = true
                emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
                value = bug.bug.solvedVersion
                isEnabled = SecurityUtils.hasPermission(app, Permission.Level.EDIT)
                addValueChangeListener { e: ComponentValueChangeEvent<Select<Version?>?, Version?> -> dataService.setBugSolved(bug.bug, e.value) }
            }
        }).setSortable(QBug.bug.solvedVersion).setCaption(Messages.SOLVED)
                .setFilterable(Translatable.createCheckbox(Messages.HIDE_SOLVED).with { value = true }, object : QueryDslFilterWithParameter<Boolean> {
                    override var parameter: Boolean = true
                    override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = if (parameter) query.where(QBug.bug.solvedVersion.isNull) else query
                })
        bugs.addOnClickNavigation(ReportTab::class.java) { it.bug.id }
        bugs.appendFooterRow().getCell(countColumn).setComponent(Translatable.createButton(Messages.MERGE_BUGS) {
            val selectedItems: List<VBug> = ArrayList(bugs.selectedItems)
            if (selectedItems.size > 1) {
                val titles = RadioButtonGroup<String>()
                titles.setItems(selectedItems.map { bug: VBug -> bug.bug.title })
                titles.value = selectedItems[0].bug.title
                FluentDialog().setTitle(Messages.CHOOSE_BUG_GROUP_TITLE).addComponent(titles).addCreateButton {
                    bugMerger.mergeBugs(selectedItems.map { it.bug }, titles.value)
                    bugs.deselectAll()
                    bugs.dataProvider.refreshAll()
                }.show()
            } else {
                Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
            }
        })
        content.removeAll()
        content.add(bugs)
        content.setFlexGrow(1.0, bugs)
    }

}