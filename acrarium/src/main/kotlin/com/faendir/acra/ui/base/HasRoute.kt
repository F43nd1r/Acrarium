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
package com.faendir.acra.ui.base

import com.faendir.acra.ui.component.Path
import com.vaadin.flow.router.AfterNavigationEvent
import org.springframework.context.ApplicationContext

/**
 * @author lukas
 * @since 18.10.18
 */
interface HasRoute : HasAcrariumTitle {
    val pathElement: Path.Element<*>

    val logicalParent: Parent<*>?
        get() = null

    fun getPathElements(applicationContext: ApplicationContext, afterNavigationEvent: AfterNavigationEvent): List<Path.Element<*>> {
        val list: MutableList<Path.Element<*>> = mutableListOf(pathElement)
        logicalParent?.let { list.addAll(it[applicationContext, afterNavigationEvent].getPathElements(applicationContext, afterNavigationEvent)) }
        return list
    }

    override val title: TranslatableText
        get() = pathElement

    fun getTranslation(key: String, vararg params: Any): String

    open class Parent<T : HasRoute>(private val parentClass: Class<T>) {
        open operator fun get(applicationContext: ApplicationContext, afterNavigationEvent: AfterNavigationEvent?): T = applicationContext.getBean(parentClass)
    }

    class ParametrizedParent<T, P>(parentClass: Class<T>, private val parameter: P) : Parent<T>(parentClass) where T : HasRoute, T : HasSecureParameter<P> {
        override fun get(applicationContext: ApplicationContext, afterNavigationEvent: AfterNavigationEvent?): T =
                super.get(applicationContext, afterNavigationEvent).apply { setParameterSecure(null, parameter) }
    }
}

inline fun <reified T : HasRoute> parent() = HasRoute.Parent(T::class.java)
inline fun <reified T, P> parent(parameter: P) where T : HasRoute, T : HasSecureParameter<P> = HasRoute.ParametrizedParent(T::class.java, parameter)