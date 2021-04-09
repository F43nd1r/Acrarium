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
package com.faendir.acra.ui.view.bug.tabs

import com.faendir.acra.model.Bug
import com.faendir.acra.navigation.BugParser
import com.faendir.acra.navigation.ParseParameter
import com.faendir.acra.ui.component.tabs.HasRoute
import com.faendir.acra.ui.component.tabs.TabView
import com.faendir.acra.ui.view.app.tabs.BugTab
import com.vaadin.flow.component.Component

/**
 * @author lukas
 * @since 19.11.18
 */
@ParseParameter(BugParser::class)
abstract class BugTab<T : Component>(bug: Bug) : TabView.TabContent<T>() {
    override val logicalParent: HasRoute.Parent<*>? = HasRoute.ParametrizedParent(BugTab::class.java, bug.app)
    override val id: Int = bug.id
    override val name: String = bug.title
}