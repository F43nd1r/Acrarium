package com.faendir.acra.ui.component

import com.faendir.acra.ui.component.Box.BoxModel
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.templatemodel.TemplateModel

/**
 * @author lukas
 * @since 27.11.19
 */
@Tag("acrarium-box")
@JsModule("./elements/box.js")
class Box(title: HasElement, details: HasElement, action: HasElement) : PolymerTemplate<BoxModel>(), HasSize, HasStyle {
    interface BoxModel : TemplateModel

    init {
        title.element.setAttribute("slot", "title")
        details.element.setAttribute("slot", "details")
        action.element.setAttribute("slot", "action")
        element.appendChild(title.element, details.element, action.element)
    }
}