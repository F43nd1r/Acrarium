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

import com.faendir.acra.dataprovider.ReportDataProvider
import com.faendir.acra.dataprovider.ReportFilter
import com.faendir.acra.dataprovider.ReportSort
import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.view.VReport
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGrid
import com.faendir.acra.ui.component.grid.ButtonRenderer
import com.faendir.acra.ui.component.grid.FilterableSortableLocalizedColumn
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
import kotlin.reflect.KMutableProperty0

/**
 * @author lukas
 * @since 17.09.18
 */
class ReportList(
    private val app: App,
    private val dataProvider: ReportDataProvider,
    private val avatarService: AvatarService,
    private val localSettings: LocalSettings,
    private val dataService: DataService,
) : Composite<ReportGridView>() {
    override fun initContent(): ReportGridView {
        return ReportGridView(dataProvider, localSettings::reportGridSettings) {
            setSelectionMode(Grid.SelectionMode.NONE)
            column(ComponentRenderer { report -> avatarService.getAvatar(report) }) {
                setSortable(ReportSort.InstallationId)
                setFilterable({ ReportFilter.InstallationId(it) }, Messages.INSTALLATION)
                setCaption(Messages.INSTALLATION)
                width = "50px"
                isAutoWidth = false
            }
            val dateColumn = column(TimeSpanRenderer { it.date }) {
                setSortable(ReportSort.Date)
                setCaption(Messages.DATE)
            }
            sort(GridSortOrder.desc(dateColumn).build())
            column({ it.versionName }) {
                setFilterable({ ReportFilter.VersionName(it) }, Messages.APP_VERSION)
                setCaption(Messages.APP_VERSION)
            }
            column({ it.androidVersion }) {
                setSortable(ReportSort.AndroidVersion)
                setFilterable({ ReportFilter.AndroidVersion(it) }, Messages.ANDROID_VERSION)
                setCaption(Messages.ANDROID_VERSION)
            }
            column(ComponentRenderer { report -> Span(report.marketingName ?: report.phoneModel).apply { element.setProperty("title", report.phoneModel) } }) {
                setFilterable({ ReportFilter.PhoneMarketingNameOrModel(it) }, Messages.DEVICE)
                setCaption(Messages.DEVICE)
            }
            column({ it.stacktrace.split("\n".toRegex(), 2).toTypedArray()[0] }) {
                setFilterable({ ReportFilter.Stacktrace(it) }, Messages.STACKTRACE)
                setCaption(Messages.STACKTRACE)
                isAutoWidth = false
                flexGrow = 1
            }
            column(ComponentRenderer { report -> Icon(if (report.isSilent) VaadinIcon.CHECK else VaadinIcon.CLOSE) }) {
                setSortable(ReportSort.IsSilent)
                setFilterable(ReportFilter.IsNotSilent, false, Messages.HIDE_SILENT)
                setCaption(Messages.SILENT)
            }
            for ((index, column) in app.configuration.customReportColumns.withIndex()) {
                column({ it.customColumns[index] }) {
                    setCaption(Messages.ONE_ARG, column)
                }
            }
            if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                column(ButtonRenderer(VaadinIcon.TRASH) { report ->
                    showFluentDialog {
                        translatableText(Messages.DELETE_REPORT_CONFIRM)
                        confirmButtons {
                            dataService.deleteReport(report)
                            dataProvider.refreshAll()
                        }
                    }
                }) {
                    setCaption(Messages.DELETE)
                    isAutoWidth = false
                    width = "100px"
                }
            }
            addOnClickNavigation(ReportView::class.java) { ReportView.getNavigationParams(it.appId, it.bugId, it.id) }
        }
    }

    @UIScope
    @SpringComponent
    class Factory(private val avatarService: AvatarService, private val localSettings: LocalSettings, private val dataService: DataService) {
        fun create(app: App, dataProvider: ReportDataProvider) =
            ReportList(app, dataProvider, avatarService, localSettings, dataService)
    }
}

class ReportGridView(
    dataProvider: ReportDataProvider,
    gridSettings: KMutableProperty0<GridSettings?>,
    initializer: BasicLayoutPersistingFilterableGrid<VReport, ReportFilter, ReportSort>.() -> Unit
) :
    AcrariumGridView<
            VReport,
            ReportFilter,
            ReportSort,
            FilterableSortableLocalizedColumn<VReport, ReportFilter, ReportSort>,
            BasicLayoutPersistingFilterableGrid<VReport, ReportFilter, ReportSort>>(
        BasicLayoutPersistingFilterableGrid(dataProvider, gridSettings.get()),
        gridSettings,
        initializer
    )