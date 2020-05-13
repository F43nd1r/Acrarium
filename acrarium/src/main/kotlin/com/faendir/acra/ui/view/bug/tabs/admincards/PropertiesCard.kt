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
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

@UIScope
@SpringComponent
class PropertiesCard(dataService: DataService) : AdminCard(dataService) {

    init {
        setHeader(Translatable.createLabel(Messages.PROPERTIES))
    }

    override fun init(bug: Bug) {
        removeContent()
        val title = Translatable.createTextArea(Messages.TITLE).with { value = bug.title }
        title.setWidthFull()
        val save = Translatable.createButton(Messages.SAVE) {
            bug.title = title.content.value
            dataService.store(bug)
        }
        val layout = FlexLayout(title, save)
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN)
        layout.alignItems = FlexComponent.Alignment.END
        add(layout)
    }
}