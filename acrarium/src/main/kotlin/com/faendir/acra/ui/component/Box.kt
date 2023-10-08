/*
 * (C) Copyright 2019-2021 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.littemplate.LitTemplate

@Tag("acrarium-box")
@JsModule("./elements/box.ts")
class Box(title: Component, details: Component, action: Component) : LitTemplate(), HasSize, HasStyle, HasSlottedComponents<Box.Slot> {
    init {
        add(Slot.TITLE, title)
        add(Slot.DETAILS, details)
        add(Slot.ACTION, action)
    }

    enum class Slot : HasSlottedComponents.Slot {
        TITLE, DETAILS, ACTION
    }
}