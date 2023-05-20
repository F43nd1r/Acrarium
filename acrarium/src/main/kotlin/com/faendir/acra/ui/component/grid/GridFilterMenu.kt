package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class GridFilterMenu(private val grid: LayoutPersistingFilterableGrid<*, *, *, *>) : PopupButton(VaadinIcon.FILTER) {
    private val content = VerticalLayout().also {
        it.style.set("background-color", "var(--lumo-shade-5pct)")
        add(it)
    }

    init {
        update()
    }

    fun update() {
        content.removeAll()
        content.add(Translatable.createLabel(Messages.FILTER).with {
            style.set("font-weight", "bold")
        })
        content.add(*grid.filterableColumns.mapNotNull { it.filterComponent }.toTypedArray())
    }
}