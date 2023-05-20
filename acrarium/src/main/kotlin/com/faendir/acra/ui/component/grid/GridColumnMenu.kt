package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.listbox.MultiSelectListBox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.TextRenderer

class GridColumnMenu(private val grid: LayoutPersistingFilterableGrid<*, *, *, *>) : PopupButton(VaadinIcon.WRENCH) {
    private val content = MultiSelectListBox<LocalizedColumn<*>>().apply {
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
        }, content).apply {
            style.set("background-color", "var(--lumo-shade-5pct)")
        })
    }

    fun update() {
        val items = grid.filterableColumns.filter { it.caption != null }
        content.setItems(items)
        content.select(items.filter { it.isVisible })
    }
}