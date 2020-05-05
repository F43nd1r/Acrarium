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
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.base.CardView
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.admincards.AdminCard
import com.faendir.acra.ui.view.app.tabs.admincards.DangerCard
import com.faendir.acra.ui.view.app.tabs.admincards.ExportCard
import com.faendir.acra.ui.view.app.tabs.admincards.NotificationCard
import com.faendir.acra.ui.view.app.tabs.admincards.VersionCard
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import javax.annotation.PostConstruct

/**
 * @author lukas
 * @since 18.10.18
 */
@UIScope
@SpringComponent
@Route(value = "admin", layout = AppView::class)
class AdminTab(dataService: DataService) : AppTab<CardView<AdminCard, App>>(dataService) {
    @PostConstruct
    fun setupContent() {
        content.add(VersionCard::class.java, NotificationCard::class.java, ExportCard::class.java, DangerCard::class.java)
    }

    override fun init(app: App) {
        content.init(app)
    }
}