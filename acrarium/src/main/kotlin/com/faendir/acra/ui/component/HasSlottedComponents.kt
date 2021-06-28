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