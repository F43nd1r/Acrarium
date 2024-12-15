/*
 * (C) Copyright 2022-2024 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.security

import com.faendir.acra.navigation.RouteParameterParser
import com.vaadin.flow.server.auth.AccessCheckResult
import com.vaadin.flow.server.auth.NavigationAccessChecker
import com.vaadin.flow.server.auth.NavigationContext
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.jvm.optionals.getOrNull

@Configuration
class AcrariumAccessAnnotationConfiguration {
    @Bean
    fun navigationAccessControlConfigurerCustomizer(): NavigationAccessControlConfigurer =
        NavigationAccessControlConfigurer().withNavigationAccessChecker(AcrariumAccessAnnotationChecker())
}

class AcrariumAccessAnnotationChecker : NavigationAccessChecker {
    override fun check(context: NavigationContext): AccessCheckResult {
        if (context.isErrorHandling) return AccessCheckResult.neutral()
        val parents = context.router.resolveNavigationTarget(context.location).getOrNull()?.routeTarget?.parentLayouts.orEmpty()
        val parameters = RouteParameterParser { context.parameters }
        return if ((listOf(context.navigationTarget) + parents).all { SecurityUtils.hasAccess(parameters::appId, it) }) {
            AccessCheckResult.allow()
        } else {
            AccessCheckResult.deny("Access denied")
        }
    }
}