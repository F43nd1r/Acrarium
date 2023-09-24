/*
 * (C) Copyright 2021-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.security

import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.login.LoginView
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
class AuthenticationUIListener(private val userRepository: UserRepository, private val routeParams: RouteParams) : UIInitListener {
    override fun uiInit(init: UIInitEvent) {
        init.ui.addBeforeEnterListener { event ->
            when (event.navigationTarget) {
                LoginView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (!userRepository.hasAnyAdmin()) {
                        event.rerouteTo(SetupView::class.java)
                    }
                }

                SetupView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (userRepository.hasAnyAdmin()) {
                        event.rerouteTo(LoginView::class.java)
                    }
                }

                else -> if (!SecurityUtils.isLoggedIn()) {
                    event.rerouteTo(if (userRepository.hasAnyAdmin()) LoginView::class.java else SetupView::class.java)
                } else if (!SecurityUtils.hasAccess(routeParams::appId, event.navigationTarget) || event.layouts.any { !SecurityUtils.hasAccess(routeParams::appId, it) }) {
                    event.rerouteToError(NotFoundException::class.java)
                }
            }
        }
    }
}