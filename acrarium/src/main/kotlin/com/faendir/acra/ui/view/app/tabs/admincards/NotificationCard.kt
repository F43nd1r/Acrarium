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
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.checkbox
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.gridLayout
import com.faendir.acra.ui.ext.translatableLabel
import com.faendir.acra.util.getCurrentUser
import com.github.appreciated.css.grid.sizes.Auto
import com.github.appreciated.css.grid.sizes.MaxContent

@View
class NotificationCard(userService: UserService, dataService: DataService, @ParseAppParameter app: App) : AdminCard(dataService) {
    init {
        content {
            setHeader(Translatable.createLabel(Messages.NOTIFICATIONS))
            gridLayout {
                setTemplateColumns(Auto(), MaxContent())
                setWidthFull()
                val user = userService.getCurrentUser()
                val settings = dataService.findMailSettings(app, user) ?: MailSettings(app, user)
                forEach(
                    listOf(
                        Messages.NEW_BUG_MAIL_LABEL to MailSettings::newBug,
                        Messages.REGRESSION_MAIL_LABEL to MailSettings::regression,
                        Messages.SPIKE_MAIL_LABEL to MailSettings::spike,
                        Messages.WEEKLY_MAIL_LABEL to MailSettings::summary
                    )
                ) { (label, property) ->
                    translatableLabel(label)
                    checkbox {
                        value = property.get(settings)
                        addValueChangeListener { event ->
                            if (event.isFromClient) {
                                property.set(settings, event.value)
                                dataService.store(settings)
                            }
                        }
                    }
                }
            }
        }
    }
}