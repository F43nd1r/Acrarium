/*
 * (C) Copyright 2017-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view

import com.faendir.acra.domain.ReportService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.AppStats
import com.faendir.acra.persistence.app.Reporter
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.dialog.closeButton
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.BugAppTab
import com.faendir.acra.ui.view.main.MainView
import com.faendir.acra.util.catching
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import org.acra.ReportField
import org.ektorp.CouchDbConnector
import org.ektorp.http.StdHttpClient
import org.ektorp.impl.StdCouchDbConnector
import org.ektorp.impl.StdCouchDbInstance
import org.json.JSONObject

/**
 * @author lukas
 * @since 13.07.18
 */
@View
@Route(value = "", layout = MainView::class)
class Overview(private val appRepository: AppRepository, private val reportService: ReportService) : Composite<VerticalLayout>(), HasAcrariumTitle {
    init {
        content {
            setSizeFull()
            val grid = basicLayoutPersistingFilterableGrid(appRepository.getProvider()) {
                setSelectionMode(Grid.SelectionMode.NONE)
                column({ it.name }) {
                    setSortable(AppStats.Sort.NAME)
                    setCaption(Messages.NAME)
                    flexGrow = 1
                }
                column({ it.bugCount }) {
                    setSortable(AppStats.Sort.BUG_COUNT)
                    setCaption(Messages.BUGS)
                }
                column({ it.reportCount }) {
                    setSortable(AppStats.Sort.REPORT_COUNT)
                    setCaption(Messages.REPORTS)
                }
                addOnClickNavigation(BugAppTab::class.java) { AppView.getNavigationParams(it.id) }
            }
            if (SecurityUtils.hasRole(Role.ADMIN)) {
                flexLayout {
                    translatableButton(Messages.NEW_APP) {
                        showFluentDialog {
                            header(Messages.NEW_APP)
                            val name = translatableTextField(Messages.NAME)
                            createButton {
                                showFluentDialog {
                                    configurationLabel(appRepository.create(name.getContent().value))
                                    closeButton()
                                }
                                grid.dataProvider.refreshAll()
                            }
                        }
                    }.with { setMarginRight(5.0, SizeUnit.PIXEL) }
                    translatableButton(Messages.IMPORT_ACRALYZER) {
                        showFluentDialog {
                            header(Messages.IMPORT_ACRALYZER)
                            val host = translatableTextField(Messages.HOST) { value = "localhost" }
                            val port = translatableNumberField(Messages.PORT) {
                                value = 5984.0
                                min = 0.0
                                max = 65535.0
                            }
                            val ssl = translatableCheckbox(Messages.SSL)
                            val databaseName = translatableTextField(Messages.DATABASE_NAME) { value = "acra-myapp" }
                            createButton {
                                val importResult = importFromAcraStorage(host.getValue(), port.getValue().toInt(), ssl.getValue(), databaseName.getValue())
                                showFluentDialog {
                                    translatableSpan(Messages.IMPORT_SUCCESS, importResult.successCount, importResult.totalCount)
                                    configurationLabel(importResult.reporter)
                                    closeButton()
                                }
                                grid.dataProvider.refreshAll()
                            }
                        }
                    }
                }
            }
        }
    }

    override val title = TranslatableText(Messages.HOME)

    private fun importFromAcraStorage(host: String, port: Int, ssl: Boolean, database: String): ImportResult {
        val httpClient = StdHttpClient.Builder().host(host).port(port).enableSSL(ssl).build()
        val db: CouchDbConnector = StdCouchDbConnector(database, StdCouchDbInstance(httpClient))
        val reporter = appRepository.create(database.replaceFirst("acra-".toRegex(), ""))
        var total = 0
        var success = 0
        for (id in db.allDocIds) {
            if (!id.startsWith("_design")) {
                total++
                catching {
                    val report = JSONObject(db.getAsStream(id).reader(Charsets.UTF_8).use { it.readText() })
                    fixStringIsArray(report, ReportField.STACK_TRACE)
                    fixStringIsArray(report, ReportField.LOGCAT)
                    reportService.create(reporter.username, report.toString(), emptyList())
                    success++
                }
            }
        }
        httpClient.shutdown()
        return ImportResult(reporter, total, success)
    }

    data class ImportResult(val reporter: Reporter, val totalCount: Int, val successCount: Int)

    private fun fixStringIsArray(report: JSONObject, reportField: ReportField) {
        report.optJSONArray(reportField.name)?.let { report.put(reportField.name, it.filterIsInstance<String>().joinToString("\n")) }
    }
}