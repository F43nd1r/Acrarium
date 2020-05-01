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
import com.faendir.acra.ui.component.Label
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.TimeSpanRenderer
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.stream.Collectors

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "bug", layout = AppView::class)
class BugTab @Autowired constructor(dataService: DataService?, private val bugMerger: BugMerger) : AppTab<VerticalLayout>(dataService) {
    override fun init(app: App) {
        content.alignItems = FlexComponent.Alignment.START
        val bugs = AcrariumGrid(dataService.getBugProvider(app))
        bugs.setSelectionMode(Grid.SelectionMode.MULTI)
        val countColumn = bugs.addColumn { it.reportCount }.setSortable(QReport.report.count()).setCaption(Messages.REPORTS)
        val dateColumn = bugs.addColumn(TimeSpanRenderer { it.lastReport }).setSortable(QReport.report.date.max()).setCaption(Messages.LATEST_REPORT)
        bugs.sort(GridSortOrder.desc(dateColumn).build())
        val versionColumn = bugs.addColumn { obj: VBug -> obj.highestVersionCode }.setSortable(QReport.report.stacktrace.version.code.max()).setCaption(Messages.LATEST_VERSION)
        bugs.addColumn { obj: VBug -> obj.userCount }.setSortable(QReport.report.installationId.countDistinct()).setCaption(Messages.AFFECTED_USERS)
        bugs.addColumn { it.bug.title }.setSortableAndFilterable(QBug.bug.title).setCaption(Messages.TITLE).setAutoWidth(false).flexGrow = 1
        val versions = dataService.findAllVersions(app)
        bugs.addColumn(ComponentRenderer<Select<Version>, VBug>(SerializableFunction { bug: VBug ->
            val versionSelect = Select(*versions.toTypedArray())
            versionSelect.setTextRenderer { obj: Version -> obj.name }
            versionSelect.isEmptySelectionAllowed = true
            versionSelect.emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
            versionSelect.value = bug.bug.solvedVersion
            versionSelect.isEnabled = SecurityUtils.hasPermission(app, Permission.Level.EDIT)
            versionSelect.addValueChangeListener { e: ComponentValueChangeEvent<Select<Version?>?, Version?> -> dataService.setBugSolved(bug.bug, e.value) }
            versionSelect
        })).setSortable(QBug.bug.solvedVersion)
                .setCaption(Messages.SOLVED)
                .setFilterable(Translatable.createCheckbox(true, Messages.HIDE_SOLVED), object : QueryDslFilterWithParameter<Boolean> {
                    override var parameter: Boolean = true

                    override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = if (parameter) query.where(QBug.bug.solvedVersion.isNull) else query

                })
        bugs.addOnClickNavigation(ReportTab::class.java) { it.bug.id }
        val merge = Translatable.createButton(ComponentEventListener {
            val selectedItems: List<VBug> = ArrayList(bugs.selectedItems)
            if (selectedItems.size > 1) {
                val titles = RadioButtonGroup<String>()
                titles.setItems(selectedItems.map { bug: VBug -> bug.bug.title })
                titles.value = selectedItems[0].bug.title
                FluentDialog().setTitle(Messages.CHOOSE_BUG_GROUP_TITLE).addComponent(titles).addCreateButton {
                    bugMerger.mergeBugs(selectedItems.stream().map { obj: VBug -> obj.bug }.collect(Collectors.toList()), titles.value)
                    bugs.deselectAll()
                    bugs.dataProvider.refreshAll()
                }.show()
            } else {
                Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
            }
        }, Messages.MERGE_BUGS)
        bugs.headerRows.last().getCell(versionColumn).setComponent(Label())
        bugs.appendFooterRow().getCell(countColumn).setComponent(merge)
        content.removeAll()
        content.add(bugs)
        content.setFlexGrow(1.0, bugs)
        content.setSizeFull()
    }

}