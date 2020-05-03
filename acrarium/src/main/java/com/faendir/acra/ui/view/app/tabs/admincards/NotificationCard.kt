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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.MailSettings
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.CssGrid
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.getCurrentUser
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.HasValue.ValueChangeListener
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.lang.NonNull

@UIScope
@SpringComponent
class NotificationCard(private val userService: UserService, dataService: DataService) : AdminCard(dataService) {

    init {
        setHeader(Translatable.createLabel(Messages.NOTIFICATIONS))
    }

    override fun init(app: App) {
        removeContent()
        val notificationLayout = CssGrid()
        notificationLayout.setTemplateColumns("auto max-content")
        notificationLayout.setWidthFull()
        val user = userService.getCurrentUser()
        val settings = dataService.findMailSettings(app, user).orElse(MailSettings(app, user))
        notificationLayout.add(Translatable.createLabel(Messages.NEW_BUG_MAIL_LABEL), Checkbox("") {
            settings.newBug = it.value
            dataService.store(settings)
        })
        notificationLayout.add(Translatable.createLabel(Messages.REGRESSION_MAIL_LABEL), Checkbox("") {
            settings.regression = it.value
            dataService.store(settings)
        })
        notificationLayout.add(Translatable.createLabel(Messages.SPIKE_MAIL_LABEL), Checkbox("") {
            settings.spike = it.value
            dataService.store(settings)
        })
        notificationLayout.add(Translatable.createLabel(Messages.WEEKLY_MAIL_LABEL), Checkbox("") {
            settings.summary = it.value
            dataService.store(settings)
        })
        if (user.mail == null) {
            val icon = VaadinIcon.WARNING.create()
            icon.style["height"] = "var(--lumo-font-size-m)"
            val div = Div(icon, Translatable.createText(Messages.NO_MAIL_SET))
            div.style["color"] = "var(--lumo-error-color)"
            div.style["font-style"] = "italic"
            notificationLayout.add(div)
        }
        add(notificationLayout)
    }
}