package com.faendir.acra.security

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import org.aopalliance.intercept.MethodInvocation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import java.util.function.Supplier

@Configuration
//@EnableMethodSecurity(prePostEnabled = true) can't enable due to https://github.com/vaadin/flow/issues/15059
class MethodSecurityConfiguration {
    @Bean
    fun customMethodSecurityExpressionHandler(): MethodSecurityExpressionHandler = object : DefaultMethodSecurityExpressionHandler() {
        override fun createSecurityExpressionRoot(authentication: Authentication, invocation: MethodInvocation) =
            CustomMethodSecurityExpressionRoot(authentication).apply {
                setThis(invocation.`this`)
                setPermissionEvaluator(permissionEvaluator)
                setTrustResolver(trustResolver)
                setRoleHierarchy(roleHierarchy)
                setDefaultRolePrefix(defaultRolePrefix)
            }
    }
}

class CustomMethodSecurityExpressionRoot(authentication: Supplier<Authentication>) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {
    private var filterObject: Any? = null

    private var returnObject: Any? = null

    private var target: Any? = null

    constructor(a: Authentication) : this({ a })

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

    fun isAdmin() = SecurityUtils.hasRole(Role.ADMIN)

    fun isUser() = SecurityUtils.hasRole(Role.USER)

    fun isReporter() = SecurityUtils.hasRole(Role.REPORTER)

    fun hasViewPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.VIEW)

    fun hasEditPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.EDIT)

    fun hasAdminPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.ADMIN)
}