package com.faendir.acra.ui.ext

import com.vaadin.flow.component.HasElement
import kotlin.reflect.KProperty

class ElementPropertyDelegate<T>(private val name: String, private val fromString: (String) -> T, private val toString: (T) -> String) {
    operator fun getValue(thisRef: HasElement, prop: KProperty<*>): T {
        return fromString(thisRef.element.getProperty(name))
    }

    operator fun setValue(thisRef: HasElement, prop: KProperty<*>, value: T) {
        thisRef.element.setProperty(name, toString(value))
    }
}

fun HasElement.booleanProperty(name: String) = ElementPropertyDelegate(name, { it.toBoolean() }, { it.toString() })

fun HasElement.stringProperty(name: String) = ElementPropertyDelegate(name, { it }, { it })