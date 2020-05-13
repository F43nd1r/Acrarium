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
import com.faendir.acra.model.User
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setWidth
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.server.VaadinService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder

class LoginLayout(private val authenticationManager: AuthenticationManager) : Composite<FlexLayout>() {
    init {
        val logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        logo.setWidth(0, Unit.PIXEL)
        val logoWrapper = FlexLayout(logo)
        logoWrapper.expand(logo)
        val loginI18n = LoginI18n.createDefault()
        loginI18n.form.title = ""
        val loginForm = LoginForm(loginI18n)
        loginForm.isForgotPasswordButtonVisible = false
        loginForm.element.style["padding"] = "0"
        loginForm.addLoginListener {
            if (!login(it.username, it.password)) {
                it.source.isError = true
            }
        }
        content.add(logoWrapper, loginForm)
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN)
        content.setSizeUndefined()
    }

    private fun login(username: String, password: String): Boolean {
        return try {
            val token = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username.toLowerCase(), password))
            if (!token.authorities.contains(User.Role.USER)) {
                throw InsufficientAuthenticationException("Missing required role")
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest())
            SecurityContextHolder.getContext().authentication = token
            UI.getCurrent().page.reload()
            true
        } catch (ex: AuthenticationException) {
            false
        }
    }
}