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

import com.vaadin.flow.component.HasStyle


fun HasStyle.preventWhiteSpaceBreaking() {
    style["white-space"] = "nowrap"
}

fun HasStyle.setMargin(value: Int, unit: Unit) {
    style["margin"] = "$value${unit.text}"
}

fun HasStyle.setMarginLeft(value: Double, unit: Unit) {
    style["margin-left"] = "$value${unit.text}"
}

fun HasStyle.setMarginTop(value: Double, unit: Unit) {
    style["margin-top"] = "$value${unit.text}"
}

fun HasStyle.setMarginRight(value: Double, unit: Unit) {
    style["margin-right"] = "$value${unit.text}"
}

fun HasStyle.setMarginBottom(value: Double, unit: Unit) {
    style["margin-bottom"] = "$value${unit.text}"
}

fun HasStyle.setDefaultTextStyle() {
    style["text-decoration"] = "none"
    style["color"] = "inherit"
}

fun HasStyle.setPadding(value: Double, unit: Unit) {
    style["padding"] = "$value${unit.text}"
}

fun HasStyle.setPaddingLeft(value: Double, unit: Unit) {
    style["padding-left"] = "$value${unit.text}"
}

fun HasStyle.setPaddingTop(value: Double, unit: Unit) {
    style["padding-top"] = "$value${unit.text}"
}

fun HasStyle.setPaddingRight(value: Double, unit: Unit) {
    style["padding-right"] = "$value${unit.text}"
}

fun HasStyle.setPaddingBottom(value: Double, unit: Unit) {
    style["padding-bottom"] = "$value${unit.text}"
}

fun HasStyle.setFlexGrow(value: Int) {
    style["flexGrow"] = "$value"
}

enum class JustifyItems(val value: String) {
    AUTO("auto"),
    NORMAL("normal"),
    START("start"),
    END("end"),
    FLEX_START("flex-start"),
    FLEX_END("flex-end"),
    SELF_START("self-start"),
    SELF_END("self-end"),
    CENTER("center"),
    LEFT("left"),
    RIGHT("right"),
    BASELINE("baseline"),
    FIRST_BASELINE("first baseline"),
    LAST_BASELINE("last baseline"),
    STRETCH("stretch")
}

fun HasStyle.setJustifyItems(justifyItems: JustifyItems) {
    style["justify-items"] = justifyItems.value
}

enum class Align(val value: String) {
    AUTO("auto"),
    NORMAL("normal"),
    START("start"),
    END("end"),
    FLEX_START("flex-start"),
    FLEX_END("flex-end"),
    SELF_START("self-start"),
    SELF_END("self-end"),
    CENTER("center"),
    BASELINE("baseline"),
    FIRST_BASELINE("first baseline"),
    LAST_BASELINE("last baseline"),
    STRETCH("stretch")
}

fun HasStyle.setAlignItems(align: Align) {
    style["align-items"] = align.value
}

fun HasStyle.setAlignSelf(align: Align) {
    style["align-self"] = align.value

}