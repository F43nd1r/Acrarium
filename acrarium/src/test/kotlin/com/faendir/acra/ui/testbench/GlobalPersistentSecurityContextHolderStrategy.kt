package com.faendir.acra.ui.testbench

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.core.context.SecurityContextImpl


@Configuration
@ConditionalOnProperty(name = ["acrarium.live"], havingValue = "false")
internal class GlobalPersistentSecurityContextHolderStrategy : SecurityContextHolderStrategy {
    companion object {
        init {
            SecurityContextHolder.setStrategyName(GlobalPersistentSecurityContextHolderStrategy::class.java.name)
        }

        private var contextHolder: SecurityContext? = null
    }


    override fun clearContext() {
        //do nothing - we want to inspect the authentication later
    }

    override fun getContext(): SecurityContext = contextHolder ?: createEmptyContext().also { contextHolder = it }

    override fun setContext(context: SecurityContext?) {
        contextHolder = context
    }

    override fun createEmptyContext(): SecurityContext = SecurityContextImpl()
}