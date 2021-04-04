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
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.util.PARAM
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.queryDslAcrariumGrid
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setMinHeight
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.IconRenderer
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Qualifier

@View
class VersionCard(dataService: DataService, @Qualifier(PARAM) app: App) : AdminCard(dataService) {

    init {
        setHeader(Translatable.createLabel(Messages.VERSIONS))
        queryDslAcrariumGrid(dataService.getVersionProvider(app)) {
            setMinHeight(280, SizeUnit.PIXEL)
            setHeight(100, SizeUnit.PERCENTAGE)
            addColumn { it.code }.setSortable(QVersion.version.code).setCaption(Messages.VERSION_CODE).flexGrow = 1
            addColumn { it.name }.setSortable(QVersion.version.name).setCaption(Messages.VERSION).flexGrow = 1
            addColumn(IconRenderer({ Icon(if (it.mappings != null) VaadinIcon.CHECK else VaadinIcon.CLOSE) }, { "" }))
                .setSortable(QVersion.version.mappings.isNotNull)
                .setCaption(Messages.PROGUARD_MAPPINGS)
            if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                addColumn(ComponentRenderer { version ->
                    Button(Icon(VaadinIcon.EDIT)) { VersionEditorDialog(dataService, app, { dataProvider.refreshAll() }, version).open() }
                })
                addColumn(ComponentRenderer { version ->
                    Button(Icon(VaadinIcon.TRASH)) {
                        FluentDialog()
                            .addComponent(Translatable.createText(Messages.DELETE_VERSION_CONFIRM, version.code))
                            .addConfirmButtons {
                                dataService.deleteVersion(version)
                                dataProvider.refreshAll()
                            }.show()
                    }
                })
                appendFooterRow().getCell(columns[0]).setComponent(
                    Translatable.createButton(Messages.NEW_VERSION) { VersionEditorDialog(dataService, app, {dataProvider.refreshAll() }).open() })
            }
        }
    }
}