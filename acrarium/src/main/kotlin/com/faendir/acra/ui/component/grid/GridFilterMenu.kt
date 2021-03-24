package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class GridFilterMenu(grid: AcrariumGrid<*>) : PopupButton(VaadinIcon.FILTER) {

    init {
        val content = VerticalLayout()
        content.add(Translatable.createLabel(Messages.FILTER).with {
            style.set("font-weight", "bold")
        })
        content.add(*grid.acrariumColumns.mapNotNull { it.filterComponent }.toTypedArray())
        add(content)
    }
}