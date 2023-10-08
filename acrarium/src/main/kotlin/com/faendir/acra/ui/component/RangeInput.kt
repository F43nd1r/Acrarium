/*
 * (C) Copyright 2018-2020 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.vaadin.flow.component.*

@Tag(Tag.INPUT)
class RangeInput : AbstractSinglePropertyField<RangeInput, Double>("value", 0.0, false), Focusable<RangeInput>, HasSize, HasStyle {

    init {
        setSynchronizedEvent("change")
        element.setProperty("type", "range")
    }

    var min: Double
        get() = get(MIN_DESCRIPTOR)
        set(min) = set(MIN_DESCRIPTOR, min)

    var max: Double
        get() = get(MAX_DESCRIPTOR)
        set(max) = set(MAX_DESCRIPTOR, max)

    var step: Double
        get() = get(STEP_DESCRIPTOR)
        set(step) = set(STEP_DESCRIPTOR, step)

    companion object {
        private val MIN_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("min", 0.0)
        private val MAX_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("max", 100.0)
        private val STEP_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("step", 1.0)
    }
}