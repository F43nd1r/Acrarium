package com.faendir.acra.ui.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.littemplate.LitTemplate

/**
 * @author lukas
 * @since 27.11.19
 */
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