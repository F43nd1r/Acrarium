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