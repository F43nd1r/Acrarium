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
package com.faendir.acra.ui.component.tabs

import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.vaadin.flow.router.AfterNavigationEvent
import org.springframework.context.support.GenericApplicationContext
import kotlin.reflect.KClass

/**
 * @author lukas
 * @since 18.10.18
 */
interface HasRoute : HasAcrariumTitle {
    val pathElement: Path.Element<*>

    val logicalParent: KClass<out HasRoute>?
        get() = null

    fun getPathElements(applicationContext: GenericApplicationContext, afterNavigationEvent: AfterNavigationEvent): List<Path.Element<*>> {
        val list: MutableList<Path.Element<*>> = mutableListOf(pathElement)
        logicalParent?.let { list.addAll(applicationContext.getBean(it.java).getPathElements(applicationContext, afterNavigationEvent)) }
        return list
    }

    override val title: TranslatableText
        get() = pathElement

    fun getTranslation(key: String, vararg params: Any): String
}
