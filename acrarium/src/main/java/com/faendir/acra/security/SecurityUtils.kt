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
import org.springframework.lang.NonNull
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun isLoggedIn(): Boolean = SecurityContextHolder.getContext().authentication?.isAuthenticated ?: false

    fun hasRole(role: User.Role): Boolean = SecurityContextHolder.getContext().authentication?.authorities?.contains(role) ?: false

    fun getUsername(): String = SecurityContextHolder.getContext().authentication?.name ?: ""

    fun hasPermission(app: App, level: Permission.Level): Boolean = SecurityContextHolder.getContext().authentication?.let {
        getPermission(app, it.authorities.filterIsInstance<Permission>()) { hasRole(User.Role.ADMIN) }.ordinal >= level.ordinal
    } ?: false

    fun getPermission(app: App, user: User): Permission.Level = getPermission(app, user.permissions) { user.roles.contains(User.Role.ADMIN) }

    private fun getPermission(app: App, permissionStream: Collection<Permission>, isAdmin: () -> Boolean): Permission.Level =
            permissionStream.firstOrNull { it.app == app }?.level ?: if (isAdmin()) Permission.Level.ADMIN else Permission.Level.NONE
}