/*
 * (C) Copyright 2022-2026 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.navigation

import com.vaadin.flow.di.Lookup
import com.vaadin.flow.di.LookupInitializer
import com.vaadin.flow.server.VaadinContext
import org.springframework.web.context.WebApplicationContext
import java.util.function.BiFunction

/**
 * this class needs to be abstract to not be picked up by vaadin
 */
abstract class NonVaadinLookupInitializer : LookupInitializer() {

    private class SpringLookup(
        private val context: WebApplicationContext,
        factory: BiFunction<Class<*>, Class<*>, Any?>,
        services: Map<Class<*>, Collection<Class<*>>>
    ) : LookupImpl(services, factory) {
        override fun <T : Any> lookup(serviceClass: Class<T>): T? {
            val beans = context.getBeansOfType(serviceClass).values

            // Check whether we have service objects instantiated without Spring
            val service = super.lookup(serviceClass)
            val allFound = if ((service == null) || beans.isNotEmpty() && service.javaClass.getPackage().name.startsWith("com.vaadin.flow")) {
                // Ignore service impl class (from the super lookup) if it's
                // absent or it's a default implementation and there are Spring
                // beans
                beans
            } else {
                beans + service
            }
            if (allFound.isEmpty()) {
                return null
            } else if (allFound.size == 1) {
                return allFound.iterator().next()
            }
            throw IllegalStateException(
                SEVERAL_IMPLS + serviceClass + SPI
                        + allFound + ONE_IMPL_REQUIRED
            )
        }

        override fun <T : Any> lookupAll(serviceClass: Class<T>): Collection<T> {
            val beans = context.getBeansOfType(serviceClass).values
            return beans + super.lookupAll(serviceClass)
        }
    }

    override fun createLookup(
        context: VaadinContext,
        services: Map<Class<*>, Collection<Class<*>>>
    ): Lookup {
        val appContext = (context as NonVaadinContext).applicationContext
        return SpringLookup(appContext, { spi, impl -> instantiate(appContext, spi, impl) }, services)
    }

    private fun <T : Any> instantiate(context: WebApplicationContext, serviceClass: Class<T>, impl: Class<*>): T? {
        val beans: Collection<T> = context.getBeansOfType(serviceClass).values
        return if (beans.any(impl::isInstance)) {
            // implementation classes found in classpath are ignored if there
            // are beans which are subclasses of these impl classes
            null
        } else {
            instantiate(serviceClass, impl)
        }
    }
}