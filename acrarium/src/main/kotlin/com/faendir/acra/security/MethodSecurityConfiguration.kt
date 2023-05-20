package com.faendir.acra.security

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.aop.support.AopUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
class MethodSecurityConfiguration {
    @Primary
    @Bean
    fun customMethodSecurityExpressionHandler(): MethodSecurityExpressionHandler = object : DefaultMethodSecurityExpressionHandler() {
        override fun createSecurityExpressionRoot(authentication: Authentication, invocation: MethodInvocation) =
            createCustomSecurityExpressionRoot({ authentication }, invocation)

        private fun createCustomSecurityExpressionRoot(
            authentication: Supplier<Authentication>,
            invocation: MethodInvocation
        ) = CustomMethodSecurityExpressionRoot(authentication).apply {
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

class CustomMethodSecurityExpressionRoot(authentication: Supplier<Authentication>) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {
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

    fun isAdmin() = SecurityUtils.hasRole(Role.ADMIN)

    fun isUser() = SecurityUtils.hasRole(Role.USER)

    fun isReporter() = SecurityUtils.hasRole(Role.REPORTER)

    fun isApi() = SecurityUtils.hasRole(Role.API)

    @JvmName("hasViewPermission")
    fun hasViewPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.VIEW)

    @JvmName("hasEditPermission")
    fun hasEditPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.EDIT)

    @JvmName("hasAdminPermission")
    fun hasAdminPermission(appId: AppId) = SecurityUtils.hasPermission(appId, Permission.Level.ADMIN)
}