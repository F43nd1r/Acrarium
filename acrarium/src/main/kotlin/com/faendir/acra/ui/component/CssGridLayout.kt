/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.ui.ext.SizeUnit
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Div

class CssGridLayout : Div() {
    init {
        style.set("display", "grid")
    }

    fun setTemplateColumns(columns: String) {
        style.set("grid-template-columns", columns)
    }

    fun setColumnGap(size: Number, unit: SizeUnit) {
        style.set("grid-column-gap", size.toString() + unit.text)
    }

    fun setRowGap(size: Number, unit: SizeUnit) {
        style.set("grid-row-gap", size.toString() + unit.text)
    }

    fun addSpanning(component: Component, span: Int) {
        add(component)
        component.element.style["grid-column"] = "span $span"
    }
}