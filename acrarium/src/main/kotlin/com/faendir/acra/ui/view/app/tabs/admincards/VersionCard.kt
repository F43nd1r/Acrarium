/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QVersion
import com.faendir.acra.model.Version
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setMinHeight
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.IconRenderer
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

@UIScope
@SpringComponent
class VersionCard(dataService: DataService) : AdminCard(dataService) {

    init {
        setHeader(Translatable.createLabel(Messages.VERSIONS))
    }

    override fun init(app: App) {
        removeContent()
        val versionGrid = AcrariumGrid(dataService.getVersionProvider(app))
        versionGrid.setMinHeight(280, Unit.PIXEL)
        versionGrid.setHeight(100, Unit.PERCENTAGE)
        versionGrid.addColumn { it.code }.setSortable(QVersion.version.code).setCaption(Messages.VERSION_CODE).flexGrow = 1
        versionGrid.addColumn { it.name }.setSortable(QVersion.version.name).setCaption(Messages.VERSION).flexGrow = 1
        versionGrid.addColumn(IconRenderer(SerializableFunction<Version, Component> { Icon(if (it.mappings != null) VaadinIcon.CHECK else VaadinIcon.CLOSE) },
                ItemLabelGenerator { "" }))
                .setSortable(QVersion.version.mappings.isNotNull)
                .setCaption(Messages.PROGUARD_MAPPINGS)
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            versionGrid.addColumn(ComponentRenderer<Button, Version> { version ->
                Button(Icon(VaadinIcon.EDIT)) { VersionEditorDialog(dataService, app, { versionGrid.dataProvider.refreshAll() }, version).open() }
            })
            versionGrid.addColumn(ComponentRenderer<Button, Version> { version ->
                Button(Icon(VaadinIcon.TRASH)) {
                    FluentDialog()
                            .addComponent(Translatable.createText(Messages.DELETE_VERSION_CONFIRM, version.code))
                            .addConfirmButtons {
                                dataService.delete(version)
                                versionGrid.dataProvider.refreshAll()
                            }.show()
                }
            })
            versionGrid.appendFooterRow().getCell(versionGrid.columns[0]).setComponent(
                    Translatable.createButton(Messages.NEW_VERSION) { VersionEditorDialog(dataService, app, { versionGrid.dataProvider.refreshAll() }).open() })
        }
        add(versionGrid)
    }
}