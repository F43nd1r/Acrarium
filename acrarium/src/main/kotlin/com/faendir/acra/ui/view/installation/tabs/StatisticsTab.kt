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
package com.faendir.acra.ui.view.installation.tabs

import com.faendir.acra.model.App
import com.faendir.acra.model.QReport
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.ParseInstallationParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.statistics.Statistics
import com.faendir.acra.ui.view.installation.InstallationView
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 11.10.18
 */
@View("installationStatisticsTab")
@Route(value = "statistics", layout = InstallationView::class)
class StatisticsTab(private val dataService: DataService, @ParseAppParameter private val app: App, @ParseInstallationParameter private val installationId: String) :
    InstallationTab<Statistics>(app, installationId) {

    override fun initContent(): Statistics {
        return Statistics(app, QReport.report.stacktrace.bug.app.eq(app).and(QReport.report.installationId.eq(installationId)), dataService)
    }
}