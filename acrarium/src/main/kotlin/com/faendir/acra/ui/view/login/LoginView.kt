/*
 * (C) Copyright 2020-2024 Lukas Morawietz (https://github.com/F43nd1r)
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
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.login.LoginView.Companion.ROUTE
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed


@JsModule("./styles/shared-styles.js")
@View
@Route(ROUTE)
@AnonymousAllowed
class LoginView :
    Composite<FlexLayout>(),
    HasAcrariumTitle, BeforeEnterObserver {
    companion object {
        const val ROUTE = "login"
    }

    private val login: LoginForm = LoginForm(LoginI18n.createDefault().apply { form.title = "" }).apply {
        isForgotPasswordButtonVisible = false
        element.style["padding"] = "0"
        action = "login"
    }

    init {
        content {
            setSizeFull()
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            flexLayout {
                flexDirection = FlexLayout.FlexDirection.COLUMN
                setSizeUndefined()
                flexLayout {
                    translatableImage("images/logo.png", Messages.ACRARIUM) {
                        setWidth(0, SizeUnit.PIXEL)
                        setFlexGrow(1)
                    }
                }
                add(login)
            }
        }
    }

    override fun beforeEnter(beforeEnterEvent: BeforeEnterEvent) {
        if (beforeEnterEvent.location.queryParameters.parameters.containsKey("error")) {
            login.isError = true
        }
    }

    override val title: TranslatableText = TranslatableText(Messages.LOGIN)
}