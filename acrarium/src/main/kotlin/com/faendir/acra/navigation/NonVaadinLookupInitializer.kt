/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.faendir.acra.navigation

import com.vaadin.flow.di.Lookup
import com.vaadin.flow.di.LookupInitializer
import com.vaadin.flow.server.VaadinContext
import org.springframework.web.context.WebApplicationContext
import java.util.function.BiFunction
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * this class needs to be abstract to not be picked up by vaadin
 */
abstract class NonVaadinLookupInitializer : LookupInitializer() {

    private class SpringLookup(
        private val context: WebApplicationContext,
        factory: BiFunction<Class<*>, Class<*>, Any?>,
        services: Map<Class<*>, Collection<Class<*>>>
    ) : LookupImpl(services, factory) {
        override fun <T> lookup(serviceClass: Class<T>): T? {
            val beans = context.getBeansOfType(serviceClass).values

            // Check whether we have service objects instantiated without Spring
            val service = super.lookup(serviceClass)
            val allFound: MutableCollection<T>
            if ((service == null) || beans.isNotEmpty() && service.javaClass
                    .getPackage().name.startsWith("com.vaadin.flow")
            ) {
                // Ignore service impl class (from the super lookup) if it's
                // absent or it's a default implementation and there are Spring
                // beans
                allFound = beans
            } else {
                allFound = ArrayList(beans.size + 1)
                allFound.addAll(beans)
                allFound.add(service)
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

        override fun <T> lookupAll(serviceClass: Class<T>): Collection<T> {
            return Stream.concat(context.getBeansOfType(serviceClass).values.stream(), super.lookupAll(serviceClass).stream())
                .collect(Collectors.toList())
        }
    }

    override fun createLookup(
        context: VaadinContext,
        services: Map<Class<*>, Collection<Class<*>>>
    ): Lookup {
        val appContext = (context as NonVaadinContext).applicationContext
        return SpringLookup(
            appContext,
            { spi: Class<*>, impl: Class<*> ->
                instantiate(appContext, spi, impl)
            }, services
        )
    }

    private fun <T> instantiate(context: WebApplicationContext, serviceClass: Class<T>, impl: Class<*>): T? {
        val beans: Collection<T> = context.getBeansOfType(serviceClass).values
        return if (beans.stream().anyMatch { bean: T -> impl.isInstance(bean) }) {
            // implementation classes found in classpath are ignored if there
            // are beans which are subclasses of these impl classes
            null
        } else {
            instantiate(serviceClass, impl)
        }
    }
}