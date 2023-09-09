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
package com.faendir.acra.ui.component

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.domain.AvatarService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.CustomColumn
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.report.ReportRow
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGridView
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.component.grid.renderer.InstantRenderer
import com.faendir.acra.ui.component.grid.renderer.VersionRenderer
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
    private val app: AppId,
    private val dataProvider: AcrariumDataProvider<ReportRow, ReportRow.Filter, ReportRow.Sort>,
    private val avatarService: AvatarService,
    private val localSettings: LocalSettings,
    private val versionRepository: VersionRepository,
    private val reportRepository: ReportRepository,
    private val customColumns: List<CustomColumn>,
) : Composite<BasicLayoutPersistingFilterableGridView<ReportRow, ReportRow.Filter, ReportRow.Sort>>() {
    override fun initContent(): BasicLayoutPersistingFilterableGridView<ReportRow, ReportRow.Filter, ReportRow.Sort> {
        return BasicLayoutPersistingFilterableGridView(dataProvider, localSettings::reportGridSettings) {
            setSelectionMode(Grid.SelectionMode.NONE)
            column(ComponentRenderer { report -> avatarService.getAvatar(report.installationId) }) {
                setSortable(ReportRow.Sort.INSTALLATION_ID)
                setFilterableContains({ ReportRow.Filter.INSTALLATION_ID(it) }, Messages.INSTALLATION)
                setCaption(Messages.INSTALLATION)
                width = "50px"
                isAutoWidth = false
            }
            val dateColumn = column(InstantRenderer { it.date }) {
                setSortable(ReportRow.Sort.DATE)
                setCaption(Messages.DATE)
            }
            sort(GridSortOrder.desc(dateColumn).build())
            val versions = versionRepository.getVersionNames(app)
            column(VersionRenderer(versions) { it.versionKey }) {
                setFilterableIs(versions, { it.name }, { ReportRow.Filter.VERSION(it.code, it.flavor) }, Messages.APP_VERSION)
                setCaption(Messages.APP_VERSION)
            }
            column({ it.androidVersion }) {
                setSortable(ReportRow.Sort.ANDROID_VERSION)
                setFilterableContains({ ReportRow.Filter.ANDROID_VERSION(it) }, Messages.ANDROID_VERSION)
                setCaption(Messages.ANDROID_VERSION)
            }
            column(ComponentRenderer { report -> Span(report.marketingDevice).apply { element.setProperty("title", report.phoneModel) } }) {
                setSortable(ReportRow.Sort.MARKETING_DEVICE)
                setFilterableContains({ ReportRow.Filter.MARKETING_DEVICE(it) }, Messages.DEVICE)
                setCaption(Messages.DEVICE)
            }
            column({ it.exceptionClass }) {
                setFilterableContains({ ReportRow.Filter.EXCEPTION_CLASS(it) }, Messages.EXCEPTION)
                setCaption(Messages.EXCEPTION)
            }
            column({ it.message }) {
                setFilterableContains({ ReportRow.Filter.MESSAGE(it) }, Messages.MESSAGE)
                setCaption(Messages.MESSAGE)
                isAutoWidth = false
                flexGrow = 1
            }
            column(ComponentRenderer { report -> Icon(if (report.isSilent) VaadinIcon.CHECK else VaadinIcon.CLOSE) }) {
                setSortable(ReportRow.Sort.IS_SILENT)
                setFilterableToggle(ReportRow.Filter.IS_NOT_SILENT, false, Messages.HIDE_SILENT)
                setCaption(Messages.SILENT)
            }
            for ((index, column) in customColumns.withIndex()) {
                column({ it.customColumns[index] }) {
                    setSortable(ReportRow.Sort.CUSTOM_COLUMN(column))
                    setFilterableContains({ ReportRow.Filter.CUSTOM_COLUMN(column, it) }, Messages.ONE_ARG, column.name)
                    setCaption(Messages.ONE_ARG, column.name)
                }
            }
            if (SecurityUtils.hasPermission(app, com.faendir.acra.persistence.user.Permission.Level.EDIT)) {
                column(ButtonRenderer(VaadinIcon.TRASH) { report ->
                    showFluentDialog {
                        translatableText(Messages.DELETE_REPORT_CONFIRM)
                        confirmButtons {
                            reportRepository.delete(app, report.id)
                            dataProvider.refreshAll()
                        }
                    }
                }) {
                    setCaption(Messages.DELETE)
                    isAutoWidth = false
                    width = "100px"
                }
            }
            addOnClickNavigation(ReportView::class.java) { ReportView.getNavigationParams(app, it.bugId, it.id) }
        }
    }

    @UIScope
    @SpringComponent
    class Factory(
        private val appRepository: AppRepository,
        private val avatarService: AvatarService,
        private val localSettings: LocalSettings,
        private val versionRepository: VersionRepository,
        private val reportRepository: ReportRepository,
    ) {
        fun create(
            app: AppId,
            getDataProvider: (ReportRepository, appId: AppId, customColumns: List<CustomColumn>) -> AcrariumDataProvider<ReportRow, ReportRow.Filter, ReportRow.Sort>
        ): ReportList {
            val customColumns = appRepository.getCustomColumns(app)
            return ReportList(
                app,
                getDataProvider(reportRepository, app, customColumns),
                avatarService,
                localSettings,
                versionRepository,
                reportRepository,
                customColumns
            )
        }
    }
}