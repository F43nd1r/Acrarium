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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.Version
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.Translatable.Companion.createSpan
import com.faendir.acra.ui.component.dialog.VersionEditorDialog
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.ext.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.IconRenderer

@View
class VersionAppAdminCard(
    private val versionRepository: VersionRepository,
    routeParams: RouteParams,
) : AdminCard() {
    private val appId = routeParams.appId()

    init {
        content {
            setHeader(createSpan(Messages.VERSIONS))
            basicLayoutPersistingFilterableGrid(versionRepository.getProvider(appId)) {
                setMinHeight(280, SizeUnit.PIXEL)
                setHeight(100, SizeUnit.PERCENTAGE)
                column({ it.code }) {
                    setSortable(Version.Sort.CODE)
                    setCaption(Messages.VERSION_CODE)
                    flexGrow = 1
                }
                column({ it.name }) {
                    setSortable(Version.Sort.NAME)
                    setCaption(Messages.VERSION)
                    flexGrow = 1
                }
                column(IconRenderer({ Icon(if (it.mappings != null) VaadinIcon.CHECK else VaadinIcon.CLOSE) }, { "" })) {
                    setSortable(Version.Sort.MAPPINGS)
                    setCaption(Messages.PROGUARD_MAPPINGS)
                }
                if (SecurityUtils.hasPermission(appId, Permission.Level.EDIT)) {
                    column(ButtonRenderer(VaadinIcon.EDIT) { VersionEditorDialog(versionRepository, appId, { dataProvider.refreshAll() }, it).open() }) {
                        key = "edit"
                        width = "50px"
                        isAutoWidth = false
                    }
                    column(ButtonRenderer(VaadinIcon.TRASH) {
                        showFluentDialog {
                            translatableText(Messages.DELETE_VERSION_CONFIRM, it.code)
                            confirmButtons {
                                versionRepository.delete(it)
                                dataProvider.refreshAll()
                            }
                        }
                    }) {
                        key = "delete"
                        width = "50px"
                        isAutoWidth = false
                    }
                    appendFooterRow().getCell(columns[0]).component =
                        Translatable.createButton(Messages.NEW_VERSION) { VersionEditorDialog(versionRepository, appId, { dataProvider.refreshAll() }).open() }
                }
            }
        }
    }
}