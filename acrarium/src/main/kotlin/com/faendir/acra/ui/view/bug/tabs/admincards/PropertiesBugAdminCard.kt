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
package com.faendir.acra.ui.view.bug.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.BugSolvedVersionSelect
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.NotFoundException

@View
@RequiresPermission(Permission.Level.EDIT)
class PropertiesBugAdminCard(
    bugRepository: BugRepository,
    versionRepository: VersionRepository,
    routeParams: RouteParams,
) : AdminCard() {
    private val appId = routeParams.appId()
    private val bugId = routeParams.bugId()
    private val bug = bugRepository.find(bugId) ?: throw NotFoundException()

    init {
        content {
            setHeader(Translatable.createLabel(Messages.PROPERTIES))
            flexLayout {
                flexDirection = FlexLayout.FlexDirection.COLUMN
                alignContent = FlexLayout.ContentAlignment.CENTER
                translatableTextArea(Messages.TITLE) {
                    value = bug.title
                    setWidthFull()
                    isClearButtonVisible
                    val saveButton = Translatable.createButton(Messages.SAVE, theme = ButtonVariant.LUMO_TERTIARY) {
                        bugRepository.setTitle(
                            appId,
                            bugId,
                            value
                        )
                        it.source.isEnabled = false
                        it.source.style["color"] = "var(--lumo-disabled-text-color)"
                    }.with {
                        setAlignSelf(Align.CENTER)
                        setPaddingLeft(0.5, SizeUnit.EM)
                        setPaddingRight(0.5, SizeUnit.EM)
                        style["color"] = "var(--lumo-disabled-text-color)"
                        isEnabled = false
                    }
                    valueChangeMode = ValueChangeMode.EAGER
                    addValueChangeListener {
                        saveButton.content.isEnabled = true
                        saveButton.content.style["color"] = "var(--lumo-primary-text-color)"
                    }
                    element.appendChild(saveButton.element.apply {
                        setAttribute("slot", "suffix")
                    })
                }
                add(
                    BugSolvedVersionSelect(
                        appId,
                        bug,
                        versionRepository.getVersionNames(appId),
                        bugRepository
                    ).apply { setTranslatableLabel(Messages.SOLVED) })
            }
        }
    }
}