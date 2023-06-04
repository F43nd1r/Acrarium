/*
 * (C) Copyright 2017-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.installation.tabs

import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.ui.component.statistics.Statistics
import com.faendir.acra.ui.view.installation.InstallationView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 11.10.18
 */
@View("installationStatisticsTab")
@Route(value = "statistics", layout = InstallationView::class)
class StatisticsTab(
    private val reportRepository: ReportRepository,
    private val versionRepository: VersionRepository,
    routeParams: RouteParams,
) : Composite<Statistics>() {
    private val appId = routeParams.appId()
    private val installationId = routeParams.installationId()
    override fun initContent(): Statistics {
        return Statistics(appId, REPORT.APP_ID.eq(appId).and(REPORT.INSTALLATION_ID.eq(installationId)), reportRepository, versionRepository)
    }
}