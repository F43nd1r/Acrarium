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

package com.faendir.acra.ui.view.login

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.User
import com.faendir.acra.navigation.View
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.ui.ext.Align
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.setAlignSelf
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setPaddingBottom
import com.faendir.acra.ui.ext.setPaddingTop
import com.faendir.acra.ui.ext.setWidth
import com.faendir.acra.ui.ext.translatableImage
import com.faendir.acra.ui.ext.translatableLabel
import com.faendir.acra.ui.ext.userEditor
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route

@JsModule("./styles/shared-styles.js")
@View
@Route(SetupView.ROUTE)
class SetupView(userService: UserService) : Composite<FlexLayout>(), HasAcrariumTitle {
    companion object {
        const val ROUTE = "setup"
    }

    init {
        if (userService.hasAdmin()) throw IllegalStateException()
        content {
            setSizeFull()
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            flexLayout {
                setFlexDirection(FlexLayout.FlexDirection.COLUMN)
                setSizeUndefined()
                flexLayout {
                    flexLayout {
                        setFlexDirection(FlexLayout.FlexDirection.COLUMN)
                        setWidth(0, SizeUnit.PIXEL)
                        setFlexGrow(1)
                        translatableLabel(Messages.WELCOME) {
                            style["font-size"] = "var(--lumo-font-size-xxl)"
                            setAlignSelf(Align.CENTER)
                        }
                        translatableImage("images/logo.png", Messages.ACRARIUM) {
                            setWidthFull()
                            setPaddingTop(0.5, SizeUnit.REM)
                            setPaddingBottom(1.0, SizeUnit.REM)
                        }
                        translatableLabel(Messages.CREATE_ADMIN)
                    }
                }
                userEditor(userService, User("", "", mutableSetOf(User.Role.ADMIN, User.Role.USER)), false) { UI.getCurrent().page.reload() }
            }
        }
    }

    override val title: TranslatableText = TranslatableText(Messages.ACRARIUM)
}