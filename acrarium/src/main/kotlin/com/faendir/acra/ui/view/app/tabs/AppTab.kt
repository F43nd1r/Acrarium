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
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.util.PARAM_APP
import com.vaadin.flow.component.Component

/**
 * @author lukas
 * @since 14.07.18
 */
abstract class AppTab<T : Component>(app: App) : TabView.TabContent<T>() {
    override val params: Map<String, String> = getNavigationParams(app)
    override val name: String = app.name

    companion object {
        fun getNavigationParams(app: App) = mapOf(PARAM_APP to app.id.toString())
    }
}