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
package com.faendir.acra.navigation

import com.vaadin.flow.component.Component
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.internal.AbstractRouteRegistry
import com.vaadin.flow.router.internal.NavigationRouteTarget
import com.vaadin.flow.router.internal.RouteTarget
import com.vaadin.flow.server.VaadinContext
import java.util.*

class SpringRouteRegistry(private val context: VaadinContext) : AbstractRouteRegistry() {

    override fun getRouteTarget(target: Class<out Component>?, parameters: RouteParameters?): RouteTarget = configuration.getRouteTarget(target, parameters)

    override fun getNavigationRouteTarget(url: String?): NavigationRouteTarget = configuration.getNavigationRouteTarget(url)

    override fun getNavigationTarget(pathString: String?): Optional<Class<out Component>> = getNavigationTarget(pathString, mutableListOf())

    override fun getNavigationTarget(pathString: String?, segments: MutableList<String>?): Optional<Class<out Component>> = configuration.getRoute(pathString, segments)

    override fun getContext(): VaadinContext = context
}