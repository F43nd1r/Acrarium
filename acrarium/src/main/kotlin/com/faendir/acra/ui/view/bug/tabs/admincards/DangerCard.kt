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
package com.faendir.acra.ui.view.bug.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.Bug
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Box
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.UI
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

@UIScope
@SpringComponent("bugDangerCard")
class DangerCard(dataService: DataService?) : AdminCard(dataService!!) {
    override fun init(bug: Bug) {
        removeContent()
        val unmergeBox = Box(Translatable.createLabel(Messages.UNMERGE_BUG), Translatable.createLabel(Messages.UNMERGE_BUG_DETAILS), Translatable.createButton(Messages.UNMERGE) {
            FluentDialog().addText(Messages.UNMERGE_BUG_CONFIRM).addConfirmButtons {
                dataService.unmergeBug(bug)
                UI.getCurrent().navigate(Overview::class.java)
            }.show()
        })
        val deleteBox = Box(Translatable.createLabel(Messages.DELETE_BUG), Translatable.createLabel(Messages.DELETE_BUG_DETAILS), Translatable.createButton(Messages.DELETE) {
            FluentDialog().addText(Messages.DELETE_BUG_CONFIRM).addConfirmButtons {
                dataService.deleteBug(bug)
                UI.getCurrent().navigate(Overview::class.java)
            }.show()
        })
        add(unmergeBox, deleteBox)
    }

    init {
        setHeader(Translatable.createLabel(Messages.DANGER_ZONE))
        setHeaderColor("var(--lumo-error-contrast-color)", "var(--lumo-error-color)")
        enableDivider()
    }
}