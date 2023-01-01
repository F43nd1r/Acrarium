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