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

import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import org.springframework.context.annotation.Configuration
import org.springframework.lang.NonNull
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.core.context.SecurityContextImpl

/**
 * A custom [SecurityContextHolderStrategy] that stores the [SecurityContext] in the Vaadin Session.
 */
@Configuration
open class VaadinSessionSecurityContextHolderStrategy : SecurityContextHolderStrategy {
    companion object {

        init {
            SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy::class.java.name)
        }

        private fun getSession(): VaadinSession = VaadinSession.getCurrent() ?: VaadinSession(VaadinService.getCurrent()).apply { VaadinSession.setCurrent(this) }

    }

    override fun clearContext() = getSession().setAttribute(SecurityContext::class.java, null)

    override fun getContext() = getSession().run { getAttribute(SecurityContext::class.java) ?: createEmptyContext().also { setAttribute(SecurityContext::class.java, it) } }

    override fun setContext(context: SecurityContext) = getSession().setAttribute(SecurityContext::class.java, context)

    override fun createEmptyContext() = SecurityContextImpl()
}