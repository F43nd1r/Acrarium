/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.navigation

import com.googlecode.gentyref.GenericTypeReflector
import com.vaadin.flow.component.Component
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.server.AmbiguousRouteConfigurationException
import com.vaadin.flow.server.startup.AbstractRouteRegistryInitializer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.stream.Stream

@Configuration
open class SpringRouteConfigurationInitializer : AbstractRouteRegistryInitializer() {
    @Bean
    open fun routeConfiguration(context: ApplicationContext): RouteConfiguration {
        val registry = SpringRouteRegistry()
        val routes = validateRouteClasses(Stream.concat(Arrays.stream(context.getBeanNamesForAnnotation(Route::class.java)),
                Arrays.stream(context.getBeanNamesForAnnotation(RouteAlias::class.java))).map { context.getType(it) })
        return RouteConfiguration.forRegistry(registry).also { it.update { setAnnotatedRoutes(it, routes) } }
    }

    private fun setAnnotatedRoutes(routeConfiguration: RouteConfiguration,
                                   routes: Set<Class<out Component?>>) {
        routeConfiguration.handledRegistry.clean()
        routes.forEach {
            try {
                routeConfiguration.setAnnotatedRoute(it)
            } catch (exception: AmbiguousRouteConfigurationException) {
                if (!handleAmbiguousRoute(routeConfiguration, exception.configuredNavigationTarget, it)) {
                    throw exception
                }
            }
        }
    }

    private fun handleAmbiguousRoute(routeConfiguration: RouteConfiguration,
                                     configuredNavigationTarget: Class<out Component?>,
                                     navigationTarget: Class<out Component?>): Boolean {
        return when {
            GenericTypeReflector.isSuperType(navigationTarget, configuredNavigationTarget) -> true
            GenericTypeReflector.isSuperType(configuredNavigationTarget, navigationTarget) -> {
                routeConfiguration.removeRoute(configuredNavigationTarget)
                routeConfiguration.setAnnotatedRoute(navigationTarget)
                true
            }
            else -> false
        }
    }
}