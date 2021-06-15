package com.faendir.acra.security

import com.faendir.acra.navigation.ParameterParser
import org.springframework.beans.factory.BeanCreationNotAllowedException
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
import org.springframework.stereotype.Component

@Component
class BeanSecurityEnforcer(private val parameterParser: ParameterParser) : InstantiationAwareBeanPostProcessor {
    override fun postProcessBeforeInstantiation(targetClass: Class<*>, beanName: String): Any? {
        if (!SecurityUtils.hasAccess(parameterParser::parseApp, targetClass)) {
            throw BeanCreationNotAllowedException(beanName, "Missing required role or permission for $targetClass")
        }
        return null
    }

}