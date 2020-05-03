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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.FlexDirection
import com.faendir.acra.ui.ext.setFlexDirection
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.FlexLayout
import org.springframework.data.util.Pair
import java.util.*

/**
 * @author Lukas
 * @since 19.12.2017
 */
class FluentDialog : AcrariumDialog() {
    private val components: MutableList<Component> = mutableListOf()
    private val fields: MutableMap<ValidatedField<*, *>, Pair<Boolean, ValidatedField.Listener>> = mutableMapOf()

    fun setTitle(titleId: String, vararg params: Any): FluentDialog {
        setHeader(titleId, *params)
        return this
    }

    fun addCreateButton(onCreateAction: FluentDialog.() -> Unit): FluentDialog {
        setPositive(Messages.CREATE) { this.onCreateAction() }
        setNegative(Messages.CANCEL)
        return this
    }

    fun addCloseButton(): FluentDialog {
        setPositive(Messages.CLOSE)
        return this
    }

    fun addYesNoButtons(onYesAction: FluentDialog.() -> Unit): FluentDialog {
        setPositive(Messages.YES) { onYesAction() }
        setNegative(Messages.NO)
        return this
    }

    fun addConfirmButtons(onYesAction: FluentDialog.() -> Unit): FluentDialog {
        setPositive(Messages.CONFIRM) { onYesAction() }
        setNegative(Messages.CANCEL)
        return this
    }

    fun addComponent(component: Component): FluentDialog {
        components.add(component)
        return this
    }

    fun addText(captionId: String, vararg params: Any): FluentDialog {
        components.add(Translatable.createText(captionId, *params))
        return this
    }

    fun addValidatedField(validatedField: ValidatedField<*, *>, isInitialValid: Boolean = false): FluentDialog {
        val listener = ValidatedField.Listener { value: Boolean -> updateField(validatedField, value) }
        validatedField.addListener(listener)
        fields[validatedField] = Pair.of(isInitialValid, listener)
        return addComponent(validatedField.field)
    }

    private fun updateField(field: ValidatedField<*, *>, value: Boolean) {
        fields[field] = Pair.of(value, fields[field]!!.second)
        checkValid()
    }

    fun show() {
        components.filterIsInstance<HasSize>().forEach {
            try {
                it.width = "100%"
            } catch (ignored: UnsupportedOperationException) {
            }
        }
        val layout = FlexLayout()
        layout.setFlexDirection(FlexDirection.COLUMN)
        components.forEach { layout.add(it) }
        checkValid()
        add(layout)
        if (!isOpened) {
            open()
        }
    }

    private fun checkValid() {
        val valid = fields.values.map { it.first }.fold(true) { a, b -> a && b }
        positive?.let { it.content.isEnabled = valid }
    }
}