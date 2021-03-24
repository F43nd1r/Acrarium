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

import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.base.ParentLayout
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager

/**
 * @author lukas
 * @since 13.07.18
 */
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/global.css")
@UIScope
@SpringComponent
class MainView(private val authenticationManager: AuthenticationManager, private val applicationContext: ApplicationContext, private val userService: UserService) :
        ParentLayout() {
    private val mainLayout: MainLayout = MainLayout(applicationContext)

    init {
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = JustifyContentMode.CENTER
        setSizeFull()
        setRouterRoot(mainLayout)
    }

    override fun onAttach(attachEvent: AttachEvent?) {
        super.onAttach(attachEvent)
        content = when {
            SecurityUtils.isLoggedIn() -> mainLayout
            userService.hasAdmin() -> LoginLayout(authenticationManager)
            else -> SetupLayout(userService)
        }
    }
}