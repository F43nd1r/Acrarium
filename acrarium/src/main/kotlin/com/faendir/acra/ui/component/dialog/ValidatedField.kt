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
package com.faendir.acra.ui.component.dialog

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import org.springframework.lang.NonNull
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * @author Lukas
 * @since 22.06.2017
 */
class ValidatedField<V, T : Component> private constructor(val field: T, private val getValue: () -> V, registerListener: ((V) -> Unit) -> Unit, private val setMessage: (String?) -> Unit) {
    private val validators: MutableMap<(V) -> Boolean, String> = mutableMapOf()
    private val listeners: MutableList<(Boolean) -> Unit> = mutableListOf()
    private var valid: Boolean = false

    init {
        registerListener { value: V -> validate(value) }
    }

    fun addValidator(validator: (V) -> Boolean, errorMessage: String): ValidatedField<V, T> {
        validators[validator] = errorMessage
        return this
    }

    fun isValid(): Boolean {
        return validate(getValue())
    }

    private fun validate(value: V): Boolean {
        val valid = validators.entries.all { it.key(value).also { valid -> setMessage(if (valid) null else it.value) } }
        if (this.valid != valid) {
            this.valid = valid
            listeners.forEach { it(valid) }
        }
        return valid
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    companion object {
        fun <V, T, E : HasValue.ValueChangeEvent<V>> of(field: T): ValidatedField<V, T> where T : Component, T : HasValue<E, V>, T : HasValidation {
            return ValidatedField(field, { field.value }, { listener -> field.addValueChangeListener {  listener(it.value) } }, { field.errorMessage = it })
        }

        fun <V, T> of(field: T, getValue: () -> V, registerListener: ((V) -> Unit) -> Unit): ValidatedField<V, T> where T : Component, T : HasValue<*, V>, T : HasValidation {
            return ValidatedField(field, getValue, registerListener) { field.errorMessage = it }
        }
    }
}