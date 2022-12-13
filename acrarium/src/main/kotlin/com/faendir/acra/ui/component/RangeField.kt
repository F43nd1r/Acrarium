/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.textfield.NumberField

/**
 * @author lukas
 * @since 07.05.19
 */
class RangeField : CustomField<Double>() {
    private val input: RangeInput = RangeInput()
    private val _field: NumberField = NumberField()

    init {
        _field.isStepButtonsVisible = true
        input.addValueChangeListener { e: ComponentValueChangeEvent<RangeInput?, Double> ->
            if (e.isFromClient) {
                _field.value = e.value
            }
        }
        _field.addValueChangeListener { e: ComponentValueChangeEvent<NumberField?, Double> ->
            if (e.isFromClient) {
                input.value = e.value
            }
        }
        add(input, _field)
    }

    override fun generateModelValue(): Double {
        return input.value
    }

    override fun setPresentationValue(newPresentationValue: Double) {
        input.value = newPresentationValue
        _field.value = newPresentationValue
    }

    var min: Double
        get() = input.min
        set(min) {
            input.min = min
            _field.min = min
        }

    var max: Double
        get() = input.max
        set(max) {
            input.max = max
            _field.max = max
        }

    var step: Double
        get() = input.step
        set(step) {
            input.step = step
            _field.step = step
        }
}