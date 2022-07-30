package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.listbox.MultiSelectListBox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.TextRenderer

class GridColumnMenu(private val grid: QueryDslAcrariumGrid<*>) : PopupButton(VaadinIcon.WRENCH) {
    private val content = MultiSelectListBox<QueryDslAcrariumColumn<*>>().apply {
        setRenderer(TextRenderer { it.caption?.translate() })
        addSelectionListener { event ->
            if (event.isFromClient) {
                event.removedSelection.forEach { it.isVisible = false }
                event.addedSelection.forEach { it.isVisible = true }
                grid.recalculateColumnWidths()
            }
        }
    }

    init {
        update()
        add(VerticalLayout(Translatable.createLabel(Messages.EDIT_COLUMNS).with {
            style.set("font-weight", "bold")
        }, content))
    }

    fun update() {
        val items = grid.acrariumColumns.filter { it.caption != null }
        content.setItems(items)
        content.select(items.filter { it.isVisible })
    }
}