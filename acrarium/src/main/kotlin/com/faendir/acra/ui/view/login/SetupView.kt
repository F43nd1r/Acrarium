/*
 * (C) Copyright 2021-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.login

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.ext.*
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

@JsModule("./styles/shared-styles.js")
@View
@Route(SetupView.ROUTE)
@AnonymousAllowed
class SetupView(userRepository: UserRepository) : Composite<FlexLayout>(), HasAcrariumTitle {
    companion object {
        const val ROUTE = "setup"
    }

    init {
        if (userRepository.hasAnyAdmin()) throw IllegalStateException()
        content {
            setSizeFull()
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            flexLayout {
                flexDirection = FlexLayout.FlexDirection.COLUMN
                setSizeUndefined()
                flexLayout {
                    flexLayout {
                        flexDirection = FlexLayout.FlexDirection.COLUMN
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
                userEditor(userRepository, mutableSetOf(Role.ADMIN, Role.USER)) { UI.getCurrent().page.reload() }
            }
        }
    }

    override val title: TranslatableText = TranslatableText(Messages.ACRARIUM)
}