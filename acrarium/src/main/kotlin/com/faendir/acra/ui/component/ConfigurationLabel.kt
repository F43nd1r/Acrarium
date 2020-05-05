/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.User
import com.faendir.acra.rest.RestReportInterface
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.util.ensureTrailing
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.server.VaadinRequest
import org.springframework.lang.NonNull

/**
 * @author lukas
 * @since 09.11.18
 */
class ConfigurationLabel(private val user: User) : Label(""), LocaleChangeObserver {
    override fun localeChange(event: LocaleChangeEvent) {
        val baseUrl = VaadinRequest.getCurrent().getHeader("host")
        element.setProperty("innerHTML", getTranslation(Messages.CONFIGURATION_LABEL,
                baseUrl.ensureTrailing("/") + RouteConfiguration.forSessionScope().getUrl(Overview::class.java),
                RestReportInterface.REPORT_PATH, user.username, user.getPlainTextPassword()))
    }

}