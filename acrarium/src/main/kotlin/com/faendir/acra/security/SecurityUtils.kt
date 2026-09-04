/*
 * (C) Copyright 2017-2026 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.vaadin.flow.server.auth.AnonymousAllowed
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.annotation.RepeatableContainers
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder


object SecurityUtils {
    @JvmStatic
    fun isLoggedIn(): Boolean = SecurityContextHolder.getContext().authentication?.takeIf { it !is AnonymousAuthenticationToken }?.isAuthenticated ?: false

    @JvmStatic
    fun hasRole(role: Role): Boolean = SecurityContextHolder.getContext().authentication?.authorities?.any { it.authority == role.authority } ?: false

    @JvmStatic
    fun getAuthorities(): Collection<GrantedAuthority> = SecurityContextHolder.getContext().authentication?.authorities ?: emptySet()

    @JvmStatic
    fun setAuthorities(authorities: Collection<GrantedAuthority>) {
        if (isLoggedIn()) {
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(getUsername(), null, authorities)
        }
    }

    @JvmStatic
    fun getUsername(): String = SecurityContextHolder.getContext().authentication?.name ?: ""

    @JvmStatic
    fun hasPermission(appId: AppId, level: Permission.Level): Boolean = SecurityContextHolder.getContext().authentication?.let { authentication ->
        (authentication.authorities.filterIsInstance<Permission>().filter { it.appId == appId }.maxByOrNull { it.level }?.level
            ?: if (hasRole(Role.ADMIN)) Permission.Level.ADMIN else Permission.Level.NONE) >= level
    } ?: false

    @JvmStatic
    fun hasAccess(getApp: () -> AppId, target: Class<*>): Boolean {
        val appId by lazy(getApp)
        return AnnotationUtils.findAnnotation(target, AnonymousAllowed::class.java) != null ||
                MergedAnnotations.from(target, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.standardRepeatables())
                    .stream(RequiresRole::class.java).allMatch { hasRole(it.synthesize().value) } &&
                MergedAnnotations.from(target, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.standardRepeatables())
                    .stream(RequiresPermission::class.java).allMatch { hasPermission(appId, it.synthesize().value) }
    }

    fun <T> withSystemAuthentication(block: () -> T): T {
        val previousContext = SecurityContextHolder.getContext()
        val systemContext = SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken("system", null, listOf(Role.ADMIN))
        }
        SecurityContextHolder.setContext(systemContext)
        return try {
            block()
        } finally {
            SecurityContextHolder.setContext(previousContext)
        }
    }
}