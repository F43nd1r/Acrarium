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
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.model.User
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.loginForm
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setWidth
import com.faendir.acra.ui.ext.translatableImage
import com.faendir.acra.ui.view.login.LoginView.Companion.ROUTE
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.VaadinServletResponse
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.DefaultSavedRequest

@JsModule("./styles/shared-styles.js")
@View
@Route(ROUTE)
class LoginView(private val authenticationManager: AuthenticationManager, private val requestCache: VaadinDefaultRequestCache) : Composite<FlexLayout>(),
    HasAcrariumTitle {
    companion object {
        const val ROUTE = "login"
    }

    init {
        content {
            setSizeFull()
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            flexLayout {
                setFlexDirection(FlexLayout.FlexDirection.COLUMN)
                setSizeUndefined()
                flexLayout {
                    translatableImage("images/logo.png", Messages.ACRARIUM) {
                        setWidth(0, SizeUnit.PIXEL)
                        setFlexGrow(1)
                    }
                }
                loginForm(LoginI18n.createDefault().apply { form.title = "" }) {
                    isForgotPasswordButtonVisible = false
                    element.style["padding"] = "0"
                    addLoginListener {
                        if (!login(it.username, it.password)) {
                            it.source.isError = true
                        }
                    }
                }
            }
        }
    }

    private fun login(username: String, password: String): Boolean {
        return try {
            val token = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username.toLowerCase(), password))
            if (!token.authorities.contains(User.Role.USER)) {
                throw InsufficientAuthenticationException("Missing required role")
            }
            SecurityContextHolder.getContext().authentication = token
            UI.getCurrent().apply {
                navigate(resolveRedirectUrl())
            }
            true
        } catch (ex: AuthenticationException) {
            false
        }
    }

    private fun resolveRedirectUrl(): String {
        val savedRequest = requestCache.getRequest(VaadinServletRequest.getCurrent().httpServletRequest, VaadinServletResponse.getCurrent().httpServletResponse)
        return (savedRequest as? DefaultSavedRequest)?.requestURI?.takeIf { it.isNotBlank() && !it.contains(LoginView.ROUTE) }?.removePrefix("/") ?: ""
    }

    override val title: TranslatableText = TranslatableText(Messages.LOGIN)
}