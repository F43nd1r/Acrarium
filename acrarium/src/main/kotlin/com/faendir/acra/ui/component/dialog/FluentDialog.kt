/*
 * (C) Copyright 2020-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.dialog

class FluentDialog : AcrariumDialog() {
    private val fields: MutableMap<ValidatedField<*, *>, Pair<Boolean, (Boolean) -> Unit>> = mutableMapOf()

    fun validatedField(validatedField: ValidatedField<*, *>, isInitialValid: Boolean = false) {
        val listener: (Boolean) -> Unit = { updateField(validatedField, it) }
        validatedField.addListener(listener)
        fields[validatedField] = Pair(isInitialValid, listener)
        add(validatedField.field)
    }

    private fun updateField(field: ValidatedField<*, *>, value: Boolean) {
        fields[field] = Pair(value, fields[field]!!.second)
        checkValid()
    }

    fun show() {
        checkValid()
        if (!isOpened) {
            open()
        }
    }

    private fun checkValid() {
        val valid = fields.values.map { it.first }.fold(true) { a, b -> a && b }
        positive?.let { it.content.isEnabled = valid }
    }
}

fun showFluentDialog(initializer: FluentDialog.() -> Unit) {
    val dialog = FluentDialog()
    dialog.initializer()
    dialog.show()
}