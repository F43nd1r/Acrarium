/*
 * (C) Copyright 2021-2024 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
class SetupUIListener(private val userRepository: UserRepository) : UIInitListener {
    override fun uiInit(init: UIInitEvent) {
        init.ui.addBeforeEnterListener { event ->
            if (event.navigationTarget != SetupView::class.java && !SecurityUtils.isLoggedIn() && !userRepository.hasAnyAdmin()) {
                event.forwardTo(SetupView::class.java)
            }
        }
    }
}