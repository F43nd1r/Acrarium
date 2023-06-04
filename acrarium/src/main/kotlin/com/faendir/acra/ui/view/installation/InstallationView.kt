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
package com.faendir.acra.ui.view.installation

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.LogicalParent
import com.faendir.acra.navigation.PARAM_APP
import com.faendir.acra.navigation.PARAM_INSTALLATION
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.ui.view.app.tabs.InstallationTab
import com.faendir.acra.ui.view.installation.tabs.ReportTab
import com.faendir.acra.ui.view.installation.tabs.StatisticsTab
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RoutePrefix
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

/**
 * @author lukas
 * @since 08.09.18
 */
@UIScope
@SpringComponent
@RoutePrefix("app/:$PARAM_APP/installation/:$PARAM_INSTALLATION")
@ParentLayout(MainView::class)
@LogicalParent(InstallationTab::class)
class InstallationView(
    routeParams: RouteParams,
) : TabView(
    routeParams.installationId(),
    TabInfo(ReportTab::class, Messages.REPORTS),
    TabInfo(StatisticsTab::class, Messages.STATISTICS),
) {

    companion object {
        fun getNavigationParams(app: AppId, installationId: String) = mapOf(PARAM_APP to app.toString(), PARAM_INSTALLATION to installationId)
    }
}