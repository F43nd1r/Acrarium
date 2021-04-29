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
import com.faendir.acra.model.QDevice
import com.faendir.acra.model.QReport
import com.faendir.acra.model.view.Queries
import com.faendir.acra.model.view.VReport
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.AvatarService
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.component.grid.ButtonRenderer
import com.faendir.acra.ui.component.grid.TimeSpanRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.translatableText
import com.faendir.acra.ui.view.report.ReportView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

/**
 * @author lukas
 * @since 17.09.18
 */
class ReportList(
    private val app: App,
    private val dataProvider: QueryDslDataProvider<VReport>,
    private val avatarService: AvatarService,
    private val localSettings: LocalSettings,
    private val deleteReport: (VReport) -> Unit
) : Composite<AcrariumGridView<VReport>>() {
    override fun initContent(): AcrariumGridView<VReport> {
        return AcrariumGridView(dataProvider, localSettings::reportGridSettings) {
            setSelectionMode(Grid.SelectionMode.NONE)
            addColumn(ComponentRenderer { report -> avatarService.getAvatar(report) }).setSortable(QReport.report.installationId)
                .setCaption(Messages.USER)
                .setWidth("50px").setAutoWidth(false)
            val dateColumn = addColumn(TimeSpanRenderer { it.date }).setSortable(QReport.report.date).setCaption(Messages.DATE)
            sort(GridSortOrder.desc(dateColumn).build())
            addColumn { it.stacktrace.version.name }.setSortable(QReport.report.stacktrace.version.code)
                .setFilterable(QReport.report.stacktrace.version.name, Messages.APP_VERSION)
                .setCaption(Messages.APP_VERSION)
            addColumn { it.androidVersion }.setSortableAndFilterable(QReport.report.androidVersion, Messages.ANDROID_VERSION)
                .setCaption(Messages.ANDROID_VERSION)
            addColumn(ComponentRenderer { report -> Span(report.marketingName ?: report.phoneModel).apply { element.setProperty("title", report.phoneModel) } })
                .setSortableAndFilterable(QDevice.device1.marketingName.coalesce(QReport.report.phoneModel).asString(), Messages.DEVICE)
                .setCaption(Messages.DEVICE)
            addColumn { it.stacktrace.stacktrace.split("\n".toRegex(), 2).toTypedArray()[0] }.setSortableAndFilterable(
                QReport.report.stacktrace.stacktrace,
                Messages.STACKTRACE
            ).setCaption(Messages.STACKTRACE).setAutoWidth(false).setFlexGrow(1)
            addColumn(ComponentRenderer { report -> Icon(if (report.isSilent) VaadinIcon.CHECK else VaadinIcon.CLOSE) })
                .setSortable(QReport.report.isSilent)
                .setFilterable(QReport.report.isSilent.eq(false), false, Messages.HIDE_SILENT)
                .setCaption(Messages.SILENT)
            for ((index, column) in app.configuration.customReportColumns.withIndex()) {
                column({ it.customColumns[index] }) {
                    setCaption(Messages.ONE_ARG, column)
                    setSortableAndFilterable(Queries.customReportColumnExpression(column), Messages.ONE_ARG, column)
                }
            }
            if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                column(ButtonRenderer(VaadinIcon.TRASH) { report ->
                    showFluentDialog {
                        translatableText(Messages.DELETE_REPORT_CONFIRM)
                        confirmButtons {
                            deleteReport(report)
                            dataProvider.refreshAll()
                        }
                    }
                }) {
                    setCaption(Messages.DELETE)
                    isAutoWidth = false
                    width = "100px"
                }
            }
            addOnClickNavigation(ReportView::class.java) { ReportView.getNavigationParams(it) }
        }
    }

    @UIScope
    @SpringComponent
    class Factory(private val avatarService: AvatarService, private val localSettings: LocalSettings) {
        fun create(app: App, dataProvider: QueryDslDataProvider<VReport>, deleteReport: (VReport) -> Unit) =
            ReportList(app, dataProvider, avatarService, localSettings, deleteReport)
    }
}