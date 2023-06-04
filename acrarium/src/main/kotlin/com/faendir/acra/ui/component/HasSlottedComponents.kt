/*
 * (C) Copyright 2021 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.Text
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.streams.asSequence

interface HasSlottedComponents<S : HasSlottedComponents.Slot> : HasElement {
    companion object {
        private const val SLOT: String = "slot"
    }

    fun add(slot: S, vararg components: Component) {
        for (component in components) {
            component.element.setAttribute(SLOT, slot.name.lowercase(Locale.getDefault()))
            element.appendChild(component.element)
        }
    }

    fun add(slot: S, text: String) {
        this.add(slot, Text(text))
    }

    fun remove(slot: S, vararg components: Component) {
        element.removeChild(*components.filter { component ->
            val parent = component.element.parent
            when {
                parent == null -> {
                    LoggerFactory.getLogger(HasComponents::class.java).debug("Remove of a component with no parent does nothing.")
                    false
                }

                component.element.getAttribute(SLOT) == slot.name.lowercase(Locale.getDefault()) -> {
                    require(this.element == parent) { "The given component ($component) is not a child of this component" }
                    true
                }

                else -> false
            }
        }.map { it.element }.toTypedArray())
    }

    fun get(slot: S): List<Component> = element.children.asSequence()
        .filter { it.getAttribute(SLOT) == slot.name.lowercase(Locale.getDefault()) }
        .mapNotNull { it.component.toNullable() }
        .toList()

    fun removeAll(slot: S) {
        element.removeChild(*element.children.filter { it.getAttribute(SLOT) == slot.name.lowercase(Locale.getDefault()) }.toArray { arrayOfNulls(it) })
    }

    interface Slot {
        val name: String
    }

}