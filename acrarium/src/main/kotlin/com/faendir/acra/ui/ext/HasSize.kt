/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.ext

import com.vaadin.flow.component.HasSize


enum class Unit(val text: String) {
    PERCENTAGE("%"), 
    PIXEL("px"), 
    REM("rem"), 
    EM("em");
}

fun HasSize.setWidth(value: Int, unit: Unit) {
    width = value.toString() + unit.text
}

fun HasSize.setMaxWidth(value: Int, unit: Unit) {
    maxWidth = value.toString() + unit.text
}

fun HasSize.setMaxWidthFull() {
    setMaxWidth(100, Unit.PERCENTAGE)
}

fun HasSize.setMinWidth(value: Int, unit: Unit) {
    minWidth = value.toString() + unit.text
}

fun HasSize.setMinWidthFull() {
    setMinWidth(100, Unit.PERCENTAGE)
}

fun HasSize.setHeight(value: Int, unit: Unit) {
    height = value.toString() + unit.text
}

fun HasSize.setMaxHeight(value: Int, unit: Unit) {
    maxHeight = value.toString() + unit.text
}

fun HasSize.setMaxHeightFull() {
    setMaxHeight(100, Unit.PERCENTAGE)
}

fun HasSize.setMinHeight(value: Int, unit: Unit) {
    minHeight =  value.toString() + unit.text
}

fun HasSize.setMinHeightFull() {
    setMinHeight(100, Unit.PERCENTAGE)
}