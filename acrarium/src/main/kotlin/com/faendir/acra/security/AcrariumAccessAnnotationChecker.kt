/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.navigation.RouteParams
import com.vaadin.flow.server.auth.AccessAnnotationChecker
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.security.Principal
import java.util.function.Function

@Component
class AcrariumAccessAnnotationChecker(private val routeParams: RouteParams) : AccessAnnotationChecker() {
    override fun hasAccess(method: Method, principal: Principal?, roleChecker: Function<String, Boolean>): Boolean {
        return SecurityUtils.hasAccess(routeParams::appId, method.declaringClass) || super.hasAccess(method, principal, roleChecker)
    }

    override fun hasAccess(cls: Class<*>, principal: Principal?, roleChecker: Function<String, Boolean>): Boolean {
        return SecurityUtils.hasAccess(routeParams::appId, cls) || super.hasAccess(cls, principal, roleChecker)
    }
}