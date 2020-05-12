/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.view.main

import com.faendir.acra.i18n.Messages
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.ext.FlexDirection
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setFlexDirection
import com.faendir.acra.ui.ext.setPaddingBottom
import com.faendir.acra.ui.ext.setPaddingTop
import com.faendir.acra.ui.ext.setWidth
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout

class SetupLayout(userService: UserService) : Composite<FlexLayout>() {
    init {
        val logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        logo.setWidthFull()
        logo.setPaddingTop(0.5, Unit.REM)
        logo.setPaddingBottom(1.0, Unit.REM)
        val welcomeLabel = Translatable.createLabel(Messages.WELCOME)
        welcomeLabel.style["font-size"] = "var(--lumo-font-size-xxl)"
        val header = FlexLayout(welcomeLabel, logo, Translatable.createLabel(Messages.CREATE_ADMIN))
        header.setFlexDirection(FlexDirection.COLUMN)
        header.setAlignSelf(FlexComponent.Alignment.CENTER, welcomeLabel)
        header.setWidth(0, Unit.PIXEL)
        val wrapper = FlexLayout(header)
        wrapper.expand(header)
        val userEditor = UserEditor(userService, null) { UI.getCurrent().page.reload() }
        content.add(wrapper, userEditor)
        content.setFlexDirection(FlexDirection.COLUMN)
        content.setSizeUndefined()
    }
}