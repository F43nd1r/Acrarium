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
import com.faendir.acra.ui.base.TabView
import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.Component

/**
 * @author lukas
 * @since 14.07.18
 */
abstract class AppTab<T : Component>(dataService: DataService) : TabView.Tab<T, App>(dataService, DataService::findApp, App::id, App::name, { null })