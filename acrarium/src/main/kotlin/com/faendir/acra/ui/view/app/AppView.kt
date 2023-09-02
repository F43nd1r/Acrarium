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
package com.faendir.acra.ui.view.app

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.PARAM_APP
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.ui.view.app.tabs.*
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RoutePrefix

/**
 * @author lukas
 * @since 13.07.18
 */
@View
@RoutePrefix("app/:$PARAM_APP")
@ParentLayout(MainView::class)
@RequiresPermission(Permission.Level.VIEW)
class AppView(
    appRepository: AppRepository,
    routeParams: RouteParams,
) : TabView(
    (appRepository.find(routeParams.appId()) ?: throw NotFoundException()).name,
    TabInfo(BugTab::class, Messages.BUGS),
    TabInfo(ReportTab::class, Messages.REPORTS),
    TabInfo(InstallationTab::class, Messages.INSTALLATIONS),
    TabInfo(StatisticsTab::class, Messages.STATISTICS),
    TabInfo(AdminTab::class, Messages.ADMIN)
) {
    companion object {
        fun getNavigationParams(appId: AppId) = mapOf(PARAM_APP to appId.toString())
    }
}