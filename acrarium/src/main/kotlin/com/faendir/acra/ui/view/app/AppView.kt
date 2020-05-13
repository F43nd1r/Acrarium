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
package com.faendir.acra.ui.view.app

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.base.HasSecureParameter.Companion.PARAM
import com.faendir.acra.ui.base.TabView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.faendir.acra.ui.view.app.tabs.AppTab
import com.faendir.acra.ui.view.app.tabs.BugTab
import com.faendir.acra.ui.view.app.tabs.ReportTab
import com.faendir.acra.ui.view.app.tabs.StatisticsTab
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RoutePrefix
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@RoutePrefix("app/:$PARAM")
@ParentLayout(MainView::class)
class AppView : TabView<AppTab<*>, Int>({ Integer.parseInt(it) }, TabInfo(BugTab::class.java, Messages.BUGS),
        TabInfo(ReportTab::class.java, Messages.REPORTS),
        TabInfo(StatisticsTab::class.java, Messages.STATISTICS),
        TabInfo(AdminTab::class.java, Messages.ADMIN)) 