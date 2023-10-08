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
package com.faendir.acra.ui.view.bug

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.*
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.ui.view.app.tabs.BugAppTab
import com.faendir.acra.ui.view.bug.tabs.AdminBugTab
import com.faendir.acra.ui.view.bug.tabs.IdentifierBugTab
import com.faendir.acra.ui.view.bug.tabs.ReportBugTab
import com.faendir.acra.ui.view.bug.tabs.StatisticsBugTab
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RoutePrefix

@View
@RoutePrefix("app/:$PARAM_APP/bug/:$PARAM_BUG")
@ParentLayout(MainView::class)
@LogicalParent(BugAppTab::class)
@RequiresPermission(Permission.Level.VIEW)
class BugView(
    bugRepository: BugRepository,
    routeParams: RouteParams,
) : TabView(
    (bugRepository.find(routeParams.bugId()) ?: throw NotFoundException()).title,
    TabInfo(ReportBugTab::class, Messages.REPORTS),
    TabInfo(IdentifierBugTab::class, Messages.STACKTRACES),
    TabInfo(StatisticsBugTab::class, Messages.STATISTICS),
    TabInfo(AdminBugTab::class, Messages.ADMIN)
) {
    companion object {
        fun getNavigationParams(appId: AppId, bugId: BugId) = mapOf(PARAM_APP to appId.toString(), PARAM_BUG to bugId.toString())
    }
}