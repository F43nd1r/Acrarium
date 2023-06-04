/*
 * (C) Copyright 2019-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.mailsettings.MailSettings
import com.faendir.acra.persistence.mailsettings.MailSettingsRepository
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.*

@View
class NotificationCard(
    mailSettingsRepository: MailSettingsRepository,
    routeParams: RouteParams,
) : AdminCard() {
    private val appId = routeParams.appId()

    init {
        content {
            setHeader(Translatable.createLabel(Messages.NOTIFICATIONS))
            gridLayout {
                setTemplateColumns("auto max-content")
                setWidthFull()
                val username = SecurityUtils.getUsername()
                val settings = mailSettingsRepository.find(appId, username) ?: MailSettings(appId, username)
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
                                mailSettingsRepository.store(settings)
                            }
                        }
                    }
                }
            }
        }
    }
}