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
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.faendir.acra.util.PARAM
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.getCurrentUser
import com.github.appreciated.css.grid.sizes.Auto
import com.github.appreciated.css.grid.sizes.MaxContent
import com.github.appreciated.layout.GridLayout
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.reflect.KMutableProperty1

@UIScope
@SpringComponent
class NotificationCard(userService: UserService, dataService: DataService, @Qualifier(PARAM) app: App) : AdminCard(dataService) {
    private val notificationLayout = GridLayout().apply {
        setTemplateColumns(Auto(), MaxContent())
        setWidthFull()
    }
    private val lines = listOf(
            Line(dataService, Messages.NEW_BUG_MAIL_LABEL, MailSettings::newBug),
            Line(dataService, Messages.REGRESSION_MAIL_LABEL, MailSettings::regression),
            Line(dataService, Messages.SPIKE_MAIL_LABEL, MailSettings::spike),
            Line(dataService, Messages.WEEKLY_MAIL_LABEL, MailSettings::summary))

    init {
        setHeader(Translatable.createLabel(Messages.NOTIFICATIONS))
        @Suppress("LeakingThis")
        add(notificationLayout)
        lines.forEach { it.addTo(notificationLayout) }
        val user = userService.getCurrentUser()
        val settings = dataService.findMailSettings(app, user) ?: MailSettings(app, user)
        lines.forEach { it.attach(settings) }
    }

    private class Line(val dataService: DataService, val label: String, val property: KMutableProperty1<MailSettings, Boolean>) {
        private var settings: MailSettings? = null
        private val checkbox = Checkbox().apply {
            addValueChangeListener { event ->
                settings?.let {
                    if (event.isFromClient) {
                        property.set(it, event.value)
                        dataService.store(it)
                    }
                }
            }
        }

        fun addTo(container: HasComponents) {
            container.add(Translatable.createLabel(label), checkbox)
        }

        fun attach(mailSettings: MailSettings) {
            settings = mailSettings
            checkbox.value = property.get(mailSettings)
        }
    }
}