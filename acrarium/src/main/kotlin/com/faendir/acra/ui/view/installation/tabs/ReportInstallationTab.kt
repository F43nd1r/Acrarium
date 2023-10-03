/*
 * (C) Copyright 2017-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.component.ReportList
import com.faendir.acra.ui.view.installation.InstallationView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 19.11.18
 */
@View
@Route(value = "report", layout = InstallationView::class)
class ReportInstallationTab(
    private val reportListFactory: ReportList.Factory,
    routeParams: RouteParams,
) : Composite<ReportList>() {
    private val appId = routeParams.appId()
    private val installationId = routeParams.installationId()

    override fun initContent(): ReportList {
        return reportListFactory.create(appId) { reportService, appId, customColumns -> reportService.getProvider(appId, installationId, customColumns) }
    }
}