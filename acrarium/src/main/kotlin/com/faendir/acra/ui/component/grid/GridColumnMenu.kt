/*
 * (C) Copyright 2021-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.PopupButton
import com.faendir.acra.ui.component.Translatable.Companion.createSpan
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
        add(VerticalLayout(createSpan(Messages.EDIT_COLUMNS).with {
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