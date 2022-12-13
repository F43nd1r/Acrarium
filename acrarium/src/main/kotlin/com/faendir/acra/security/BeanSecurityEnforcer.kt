package com.faendir.acra.security

import com.faendir.acra.navigation.RouteParams
import org.springframework.beans.factory.BeanCreationNotAllowedException
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
import org.springframework.stereotype.Component

@Component
class BeanSecurityEnforcer(private val routeParams: RouteParams) : InstantiationAwareBeanPostProcessor {
    override fun postProcessBeforeInstantiation(targetClass: Class<*>, beanName: String): Any? {
        if (!SecurityUtils.hasAccess(routeParams::appId, targetClass)) {
            throw BeanCreationNotAllowedException(beanName, "Missing required role or permission for $targetClass")
        }
        return null
    }

}