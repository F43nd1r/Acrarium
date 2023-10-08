/*
 * (C) Copyright 2018-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.app.Reporter
import com.faendir.acra.rest.RestReportInterface
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.util.ensureTrailing
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinService
import org.springframework.beans.factory.annotation.Value

class ConfigurationLabel private constructor(
    @Value("\${server.context-path}")
    private val baseUrl: String?
) : Span(""), LocaleChangeObserver {
    private lateinit var username: String
    private lateinit var rawPassword: String

    override fun localeChange(event: LocaleChangeEvent) {
        val baseUrl = this.baseUrl ?: VaadinRequest.getCurrent().getHeader("host")
        element.setProperty(
            "innerHTML", getTranslation(
                Messages.CONFIGURATION_LABEL,
                baseUrl.ensureTrailing("/") + RouteConfiguration.forSessionScope().getUrl(Overview::class.java),
                RestReportInterface.REPORT_PATH, username, rawPassword
            )
        )
    }

    companion object {
        fun forReporter(reporter: Reporter): ConfigurationLabel =
            VaadinService.getCurrent().instantiator.createComponent(ConfigurationLabel::class.java).also { it.username = reporter.username; it.rawPassword = reporter.rawPassword }
    }

}