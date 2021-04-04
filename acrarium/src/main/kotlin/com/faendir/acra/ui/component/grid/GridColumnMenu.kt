package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.listbox.MultiSelectListBox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.TextRenderer

class GridColumnMenu(grid: QueryDslAcrariumGrid<*>) : PopupButton(VaadinIcon.WRENCH) {

    init {
        val content = MultiSelectListBox<QueryDslAcrariumColumn<*>>()
        content.setRenderer(TextRenderer{ it.caption?.translate() })
        content.addSelectionListener { event ->
            event.addedSelection.forEach { it.isVisible = true }
            event.removedSelection.forEach { it.isVisible = false }
            grid.recalculateColumnWidths()
        }
        val items = grid.acrariumColumns.filter { it.caption != null }
        content.setItems(items)
        content.select(items.filter { it.isVisible })
        add(VerticalLayout(Translatable.createLabel(Messages.EDIT_COLUMNS).with {
            style.set("font-weight","bold")
        }, content))
    }
}