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
package com.faendir.acra.ui.component

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QReport
import com.faendir.acra.model.Report
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.AvatarService
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.view.report.ReportView
import com.faendir.acra.util.TimeSpanRenderer
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.ValueProvider

/**
 * @author lukas
 * @since 17.09.18
 */
class ReportList(app: App, private val dataProvider: QueryDslDataProvider<Report>, avatarService: AvatarService, deleteReport: (Report) -> Unit) : Composite<AcrariumGrid<Report>>() {
    init {
        content.run {
            setSelectionMode(Grid.SelectionMode.NONE)
            val userColumn = addColumn(ComponentRenderer { report: Report -> avatarService.getAvatar(report) }).setSortable(QReport.report.installationId).setCaption(Messages.USER)
            //workaround for auto column sizing
            filterRow.getCell(userColumn).setComponent(Div())
            val dateColumn = addColumn(TimeSpanRenderer { it.date }).setSortable(QReport.report.date).setCaption(Messages.DATE)
            sort(GridSortOrder.desc(dateColumn).build())
            addColumn { it.stacktrace.version.code }.setSortableAndFilterable(QReport.report.stacktrace.version.code).setCaption(Messages.APP_VERSION)
            addColumn { it.androidVersion }.setSortableAndFilterable(QReport.report.androidVersion).setCaption(Messages.ANDROID_VERSION)
            addColumn { it.phoneModel }.setSortableAndFilterable(QReport.report.phoneModel).setCaption(Messages.DEVICE)
            addColumn { it.stacktrace.stacktrace.split("\n".toRegex(), 2).toTypedArray()[0] }.setSortableAndFilterable(QReport.report.stacktrace.stacktrace)
                    .setCaption(Messages.STACKTRACE).setAutoWidth(false).flexGrow = 1
            if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                addColumn(ComponentRenderer { report: Report ->
                    Button(Icon(VaadinIcon.TRASH)) {
                        FluentDialog().addText(Messages.DELETE_REPORT_CONFIRM).addConfirmButtons {
                            deleteReport(report)
                            dataProvider.refreshAll()
                        }.show()
                    }
                })
            }
            addOnClickNavigation(ReportView::class.java) { it.id }
        }
    }

    override fun initContent(): AcrariumGrid<Report> {
        return AcrariumGrid(dataProvider)
    }
}