/*
 * (C) Copyright 2018-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.navigation.View
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.CardView
import com.faendir.acra.ui.component.SpringComposite
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.admincards.*
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 18.10.18
 */
@View
@Route(value = "admin", layout = AppView::class)
class AdminTab : SpringComposite<CardView<AdminCard>>() {
    init {
        content.add(VersionCard::class, NotificationCard::class, ExportCard::class, CustomColumnCard::class, DangerCard::class)
    }
}