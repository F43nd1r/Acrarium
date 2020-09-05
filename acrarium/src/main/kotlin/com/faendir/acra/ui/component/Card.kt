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
package com.faendir.acra.ui.component

import com.faendir.acra.ui.component.Card.CardModel
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.templatemodel.TemplateModel

/**
 * @author lukas
 * @since 18.10.18
 */
@Tag("acrarium-card")
@JsModule("./elements/card.js")
open class Card() : PolymerTemplate<CardModel>(), HasSize, HasStyle, HasComponents {
    constructor(vararg components: Component?) : this() {
        add(*components)
    }

    fun setHeader(vararg components: Component) {
        components.forEach { it.element.setAttribute("slot", "header") }
        add(*components)
    }

    var allowCollapse by model::canCollapse

    fun collapse() {
        model.collapse = true
    }

    fun expand() {
        model.collapse = false
    }

    val isCollapsed: Boolean
        get() = model.collapse

    fun enableDivider() {
        model.divider = true
    }

    fun setHeaderColor(textColor: String?, backgroundColor: String?) {
        style["--acrarium-card-header-text-color"] = textColor
        style["--acrarium-card-header-color"] = backgroundColor
    }

    fun removeContent() {
        children.filter { it.element.getAttribute("slot") == null }.forEach { this.remove(it) }
    }

    interface CardModel : TemplateModel {
        var canCollapse: Boolean
        var collapse: Boolean
        var divider: Boolean
    }
}