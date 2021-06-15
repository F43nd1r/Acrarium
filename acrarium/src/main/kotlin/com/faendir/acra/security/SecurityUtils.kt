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
package com.faendir.acra.security

import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.User
import com.vaadin.flow.server.HandlerHelper.RequestType
import com.vaadin.flow.shared.ApplicationConstants
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletRequest


object SecurityUtils {
    @JvmStatic
    fun isLoggedIn(): Boolean = SecurityContextHolder.getContext().authentication?.takeIf { it !is AnonymousAuthenticationToken }?.isAuthenticated ?: false

    @JvmStatic
    fun hasRole(role: User.Role): Boolean = SecurityContextHolder.getContext().authentication?.authorities?.any { it.authority == role.authority } ?: false

    @JvmStatic
    fun getUsername(): String = SecurityContextHolder.getContext().authentication?.name ?: ""

    @JvmStatic
    fun hasPermission(app: App, level: Permission.Level): Boolean = SecurityContextHolder.getContext().authentication?.let {
        getPermission(app, it.authorities.filterIsInstance<Permission>()) { hasRole(User.Role.ADMIN) }.ordinal >= level.ordinal
    } ?: false

    @JvmStatic
    fun hasAccess(getApp: () -> App, target: Class<*>): Boolean {
        return AnnotationUtils.findAnnotation(target, RequiresRole::class.java)?.let { hasRole(it.value) } ?: true &&
                AnnotationUtils.findAnnotation(target, RequiresPermission::class.java)?.let { hasPermission(getApp(), it.value) } ?: true
    }

    @JvmStatic
    fun getPermission(app: App, user: User): Permission.Level = getPermission(app, user.permissions) { user.roles.contains(User.Role.ADMIN) }

    private fun getPermission(app: App, permissionStream: Collection<Permission>, isAdmin: () -> Boolean): Permission.Level =
        permissionStream.firstOrNull { it.app == app }?.level ?: if (isAdmin()) Permission.Level.ADMIN else Permission.Level.NONE


    @JvmStatic
    fun isFrameworkInternalRequest(request: HttpServletRequest): Boolean {
        val parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)
        return (parameterValue != null && RequestType.values().any { it.identifier == parameterValue })
    }
}