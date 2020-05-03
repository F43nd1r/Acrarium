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
package com.faendir.acra.ui.view

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.base.HasAcrariumTitle
import com.faendir.acra.ui.base.TranslatableText
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.LocalSettings
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.beans.factory.annotation.Autowired
import java.util.function.Consumer

/**
 * @author lukas
 * @since 10.09.19
 */
@UIScope
@SpringComponent
@Route(value = "settings", layout = MainView::class)
class SettingsView(localSettings: LocalSettings) : FlexLayout(), HasAcrariumTitle {

    init {
        setSizeFull()
        justifyContentMode = JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER
        val layout = FormLayout()
        layout.setResponsiveSteps(ResponsiveStep("0px", 1))
        layout.style["align-self"] = "auto"
        layout.add(Translatable.createCheckbox(Messages.DARK_THEME).with {
            value = localSettings.darkTheme
            addValueChangeListener {
                localSettings.darkTheme = it.value
                VaadinSession.getCurrent().uIs.forEach(Consumer { ui: UI -> ui.element.setAttribute("theme", if (it.value) Lumo.DARK else Lumo.LIGHT) })
            }
        }, Translatable.createSelect(LocalSettings.getI18NProvider().providedLocales, Messages.LOCALE).with {
            setItemLabelGenerator { it.getDisplayName(localSettings.locale) }
            value = localSettings.locale
            addValueChangeListener {
                localSettings.locale = it.value
                VaadinSession.getCurrent().locale = it.value
            }
        })
        add(layout)
    }

    override fun getTitle(): TranslatableText = TranslatableText(Messages.SETTINGS)
}