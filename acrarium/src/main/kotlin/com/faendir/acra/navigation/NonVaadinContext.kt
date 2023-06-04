/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.vaadin.flow.server.VaadinContext
import org.springframework.web.context.WebApplicationContext
import java.util.*
import java.util.function.Supplier

class NonVaadinContext(val applicationContext: WebApplicationContext) : VaadinContext {
    private val attributes = mutableMapOf<Class<*>, Any>()

    override fun <T : Any> getAttribute(type: Class<T>, defaultValueSupplier: Supplier<T>?): T {
        return try {
            type.cast(attributes[type])
        } catch (e: ClassCastException) {
            null
        } ?: defaultValueSupplier?.get() ?: throw NoSuchElementException()
    }

    override fun <T : Any> setAttribute(clazz: Class<T>, value: T) {
        attributes[clazz] = value
    }

    override fun removeAttribute(clazz: Class<*>?) {
        attributes.remove(clazz)
    }

    override fun getContextParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getContextParameter(name: String?): String? = null
}