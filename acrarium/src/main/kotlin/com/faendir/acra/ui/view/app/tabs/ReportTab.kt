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
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.model.App
import com.faendir.acra.model.Report
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.ReportList
import com.faendir.acra.ui.view.app.AppView
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.lang.NonNull
import java.util.function.Consumer

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = AppView::class)
class ReportTab(dataService: DataService, private val avatarService: AvatarService, private val localSettings: LocalSettings) : AppTab<Div>(dataService) {

    init {
        content.setSizeFull()
    }

    override fun init(app: App) {
        content.removeAll()
        content.add(ReportList(app, dataService.getReportProvider(app), avatarService, localSettings) { dataService.deleteReport(it) })
    }
}