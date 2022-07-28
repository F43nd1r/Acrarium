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
package com.faendir.acra.ui.theme

import com.faendir.acra.settings.LocalSettings
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.VaadinSessionScope
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Lazy

@SpringComponent
class ThemeUIListener(private val uiThemeSetter: ObjectProvider<UIThemeSetter>) : UIInitListener {
    override fun uiInit(event: UIInitEvent) {
        uiThemeSetter.ifAvailable { it.uiInit(event) }
    }
}

@VaadinSessionScope
@SpringComponent
@Lazy
class UIThemeSetter(private val localSettings: LocalSettings) {
    fun uiInit(event: UIInitEvent) {
        event.ui.element.setAttribute("theme", if (localSettings.darkTheme) Lumo.DARK else Lumo.LIGHT)
        event.ui.locale = localSettings.locale
    }
}