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
import com.faendir.acra.navigation.View
import com.faendir.acra.service.DataService
import com.faendir.acra.util.PARAM
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.translatableButton
import com.faendir.acra.ui.ext.translatableTextArea
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Qualifier

@View
class PropertiesCard(dataService: DataService, @Qualifier(PARAM) bug: Bug) : AdminCard(dataService) {

    init {
        setHeader(Translatable.createLabel(Messages.PROPERTIES))
        flexLayout {
            setFlexDirection(FlexLayout.FlexDirection.COLUMN)
            alignItems = FlexComponent.Alignment.END
            val title = translatableTextArea(Messages.TITLE) {
                value = bug.title
                setWidthFull()
            }
            translatableButton(Messages.SAVE) {
                bug.title = title.content.value
                dataService.store(bug)
            }
        }
    }
}