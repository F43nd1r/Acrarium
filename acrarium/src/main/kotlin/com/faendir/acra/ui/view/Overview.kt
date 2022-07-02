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
package com.faendir.acra.ui.view

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.model.QApp
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QReport
import com.faendir.acra.model.User
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.dialog.closeButton
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.app.tabs.AppTab
import com.faendir.acra.ui.view.app.tabs.BugTab
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 13.07.18
 */
@View
@Route(value = "", layout = MainView::class)
class Overview(private val dataService: DataService) : Composite<VerticalLayout>(), HasAcrariumTitle {
    init {
        content {
            setSizeFull()
            val grid = queryDslAcrariumGrid(dataService.getAppProvider()) {
                setSelectionMode(Grid.SelectionMode.NONE)
                column({ it.name }) {
                    setSortable(QApp.app.name)
                    setCaption(Messages.NAME)
                    flexGrow = 1
                }
                column({ it.bugCount }) {
                    setSortable(QBug.bug.countDistinct())
                    setCaption(Messages.BUGS)
                }
                column({ it.reportCount }) {
                    setSortable(QReport.report.count())
                    setCaption(Messages.REPORTS)
                }
                addOnClickNavigation(BugTab::class.java) { AppTab.getNavigationParams(it.app) }
            }
            if (SecurityUtils.hasRole(User.Role.ADMIN)) {
                flexLayout {
                    translatableButton(Messages.NEW_APP) {
                        showFluentDialog {
                            header(Messages.NEW_APP)
                            val name = translatableTextField(Messages.NAME)
                            createButton {
                                showFluentDialog {
                                    configurationLabel(dataService.createNewApp(name.getContent().value))
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
                                val importResult = dataService.importFromAcraStorage(host.getValue(), port.getValue().toInt(), ssl.getValue(), databaseName.getValue())
                                showFluentDialog {
                                    translatableLabel(Messages.IMPORT_SUCCESS, importResult.successCount, importResult.totalCount)
                                    configurationLabel(importResult.user)
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
}