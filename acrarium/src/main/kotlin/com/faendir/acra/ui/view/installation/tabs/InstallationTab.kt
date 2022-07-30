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
package com.faendir.acra.ui.view.installation.tabs

import com.faendir.acra.model.App
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.ui.view.app.tabs.InstallationTab
import com.faendir.acra.util.PARAM_APP
import com.faendir.acra.util.PARAM_INSTALLATION
import com.vaadin.flow.component.Component

/**
 * @author lukas
 * @since 19.11.18
 */

abstract class InstallationTab<T : Component>(app: App, installationId: String) : TabView.TabContent<T>() {
    override val logicalParent = InstallationTab::class
    override val params = getNavigationParams(app, installationId)
    override val name = installationId

    companion object {
        fun getNavigationParams(app: App, installationId: String) = mapOf(PARAM_APP to app.id.toString(), PARAM_INSTALLATION to installationId)
    }
}