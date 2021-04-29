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
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.ButtonRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.queryDslAcrariumGrid
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setMinHeight
import com.faendir.acra.ui.ext.translatableText
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.IconRenderer

@View
class VersionCard(dataService: DataService, @ParseAppParameter app: App) : AdminCard(dataService) {
    init {
        content {
            setHeader(Translatable.createLabel(Messages.VERSIONS))
            queryDslAcrariumGrid(dataService.getVersionProvider(app)) {
                setMinHeight(280, SizeUnit.PIXEL)
                setHeight(100, SizeUnit.PERCENTAGE)
                column({ it.code }) {
                    setSortable(QVersion.version.code)
                    setCaption(Messages.VERSION_CODE)
                    flexGrow = 1
                }
                column({ it.name }) {
                    setSortable(QVersion.version.name)
                    setCaption(Messages.VERSION)
                    flexGrow = 1
                }
                column(IconRenderer({ Icon(if (it.mappings != null) VaadinIcon.CHECK else VaadinIcon.CLOSE) }, { "" })) {
                    setSortable(QVersion.version.mappings.isNotNull)
                    setCaption(Messages.PROGUARD_MAPPINGS)
                }
                if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                    column(ButtonRenderer(VaadinIcon.EDIT) { VersionEditorDialog(dataService, app, { dataProvider.refreshAll() }, it).open() }) {
                        width = "50px"
                        isAutoWidth = false
                    }
                    column(ButtonRenderer(VaadinIcon.TRASH) {
                        showFluentDialog {
                            translatableText(Messages.DELETE_VERSION_CONFIRM, it.code)
                            confirmButtons {
                                dataService.deleteVersion(it)
                                dataProvider.refreshAll()
                            }
                        }
                    }) {
                        width = "50px"
                        isAutoWidth = false
                    }
                    appendFooterRow().getCell(columns[0]).setComponent(
                        Translatable.createButton(Messages.NEW_VERSION) { VersionEditorDialog(dataService, app, { dataProvider.refreshAll() }).open() })
                }
            }
        }
    }
}