/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
import com.faendir.acra.persistence.user.UserRepository
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.aop.support.AopUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import org.springframework.context.expression.MethodBasedEvaluationContext
import org.springframework.expression.EvaluationContext
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.Authentication
import java.util.function.Supplier

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class MethodSecurityConfiguration(@Lazy private val userRepository: UserRepository) {
    @Primary
    @Bean
    fun customMethodSecurityExpressionHandler(): MethodSecurityExpressionHandler = object : DefaultMethodSecurityExpressionHandler() {
        override fun createSecurityExpressionRoot(authentication: Authentication, invocation: MethodInvocation) =
            createCustomSecurityExpressionRoot({ authentication }, invocation)

        private fun createCustomSecurityExpressionRoot(
            authentication: Supplier<Authentication>,
            invocation: MethodInvocation
        ) = CustomMethodSecurityExpressionRoot(authentication, userRepository).apply {
            setThis(invocation.`this`)
            setPermissionEvaluator(permissionEvaluator)
            setTrustResolver(trustResolver)
            setRoleHierarchy(roleHierarchy)
            setDefaultRolePrefix(defaultRolePrefix)
        }

        private fun getSpecificMethod(mi: MethodInvocation) = AopUtils.getMostSpecificMethod(mi.method, mi.getThis()?.let { AopProxyUtils.ultimateTargetClass(it) })

        override fun createEvaluationContext(authentication: Supplier<Authentication>, mi: MethodInvocation): EvaluationContext {
            val root = createCustomSecurityExpressionRoot(authentication, mi)
            val ctx = MethodBasedEvaluationContext(root, getSpecificMethod(mi), mi.arguments, parameterNameDiscoverer)
            ctx.beanResolver = beanResolver
            return ctx
        }
    }
}

class CustomMethodSecurityExpressionRoot(
    authentication: Supplier<Authentication>,
    private val userRepository: UserRepository
) : SecurityExpressionRoot(authentication),
    MethodSecurityExpressionOperations {
    private var filterObject: Any? = null

    private var returnObject: Any? = null

    private var target: Any? = null

    override fun setFilterObject(filterObject: Any?) {
        this.filterObject = filterObject
    }

    override fun getFilterObject(): Any? {
        return filterObject
    }

    override fun setReturnObject(returnObject: Any?) {
        this.returnObject = returnObject
    }

    override fun getReturnObject(): Any? {
        return returnObject
    }

    fun setThis(target: Any?) {
        this.target = target
    }

    override fun getThis(): Any? {
        return target
    }

    fun hasNoAdmins() = !userRepository.hasAnyAdmin()

    fun isAdmin() = SecurityUtils.hasRole(Role.ADMIN)

    fun isUser() = SecurityUtils.hasRole(Role.USER)

    fun isReporter() = SecurityUtils.hasRole(Role.REPORTER)

    fun isApi() = SecurityUtils.hasRole(Role.API)

    fun isCurrentUser(username: String) = SecurityUtils.getUsername() == username

    @JvmName("hasViewPermission")
    fun hasViewPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.VIEW)

    @JvmName("hasEditPermission")
    fun hasEditPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.EDIT)

    @JvmName("hasAdminPermission")
    fun hasAdminPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.ADMIN)
}