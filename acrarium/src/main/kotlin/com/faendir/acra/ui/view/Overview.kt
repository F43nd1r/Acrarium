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
import com.faendir.acra.model.QApp
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QReport
import com.faendir.acra.model.User
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.base.HasAcrariumTitle
import com.faendir.acra.ui.base.TranslatableText
import com.faendir.acra.ui.component.ConfigurationLabel
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.dialog.ValidatedField
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.view.app.tabs.BugTab
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@Route(value = "", layout = MainView::class)
class Overview(private val dataService: DataService) : VerticalLayout(), ComponentEventListener<AttachEvent>, HasAcrariumTitle {

    init {
        addAttachListener(this)
    }

    override fun onComponentEvent(event: AttachEvent) {
        removeAll()
        val grid = AcrariumGrid(dataService.getAppProvider())
        grid.setSelectionMode(Grid.SelectionMode.NONE)
        val appColumn = grid.addColumn { it.name }.setSortable(QApp.app.name).setCaption(Messages.NAME).setFlexGrow(1)
        grid.addColumn { it.bugCount }.setSortable(QBug.bug.countDistinct()).setCaption(Messages.BUGS)
        grid.addColumn { it.reportCount }.setSortable(QReport.report.count()).setCaption(Messages.REPORTS)
        grid.addOnClickNavigation(BugTab::class.java) { it.id }
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            grid.appendFooterRow().getCell(appColumn).setComponent(HorizontalLayout(Translatable.createButton(Messages.NEW_APP) {
                val name = Translatable.createTextField(Messages.NAME)
                FluentDialog().setTitle(Messages.NEW_APP).addComponent(name).addCreateButton {
                    FluentDialog().addComponent(ConfigurationLabel(dataService.createNewApp(name.content.value))).addCloseButton().show()
                    grid.dataProvider.refreshAll()
                }.show()
            }, Translatable.createButton(Messages.IMPORT_ACRALYZER) {
                val host = Translatable.createTextField(Messages.HOST).with { value = "localhost" }
                val port = Translatable.createNumberField(Messages.PORT).with {
                    value = 5984.0
                    min = 0.0
                    max = 65535.0
                }
                val ssl = Translatable.createCheckbox(Messages.SSL)
                val databaseName = Translatable.createTextField(Messages.DATABASE_NAME).with { value = "acra-myapp" }
                FluentDialog().setTitle(Messages.IMPORT_ACRALYZER)
                        .addComponent(host)
                        .addComponent(port)
                        .addComponent(ssl)
                        .addValidatedField(ValidatedField.of(databaseName), true)
                        .addCreateButton {
                            val importResult = dataService.importFromAcraStorage(host.content.value, port.value.toInt(), ssl.content.value, databaseName.content.value)
                            FluentDialog().addComponent(Translatable.createLabel(Messages.IMPORT_SUCCESS, importResult.successCount, importResult.totalCount))
                                    .addComponent(ConfigurationLabel(importResult.user))
                                    .addCloseButton()
                                    .show()
                            grid.dataProvider.refreshAll()
                        }.show()
            }))
        }
        setSizeFull()
        add(grid)
    }

    override val title = TranslatableText(Messages.ACRARIUM)
}