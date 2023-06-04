/*
 * (C) Copyright 2021-2022 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setMarginRight
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.reflect.KMutableProperty0

open class AcrariumGridView<T : Any, F : Any, S : Any, C : FilterableSortableLocalizedColumn<T, F, S>, G : LayoutPersistingFilterableGrid<T, F, S, C>>(
    layoutPersistingFilterableGrid: G,
    gridSettings: KMutableProperty0<GridSettings?>,
    initializer: G.() -> Unit = {}
) : VerticalLayout() {

    val grid: G = layoutPersistingFilterableGrid.apply {
        initializer()
        loadLayout()
        addOnLayoutChangedListener { gridSettings.set(it) }
    }
    private val filterMenu = GridFilterMenu(grid).apply { content.setMarginRight(5.0, com.faendir.acra.ui.ext.SizeUnit.PIXEL) }
    private val columnMenu = GridColumnMenu(grid)
    val header = FlexLayout(
        Div().apply { setFlexGrow(1) }, //spacer
        filterMenu,
        columnMenu
    )

    init {
        header.setWidthFull()
        add(header, grid)
        setFlexGrow(1.0, grid)
        setSizeFull()
        updateHeader()
    }

    internal fun updateHeader() {
        filterMenu.update()
        columnMenu.update()
    }
}

class BasicLayoutPersistingFilterableGridView<T : Any, F : Any, S : Any>(
    dataProvider: AcrariumDataProvider<T, F, S>,
    gridSettings: KMutableProperty0<GridSettings?>,
    initializer: BasicLayoutPersistingFilterableGrid<T, F, S>.() -> Unit
) : AcrariumGridView<T, F, S, FilterableSortableLocalizedColumn<T, F, S>, BasicLayoutPersistingFilterableGrid<T, F, S>>(
    BasicLayoutPersistingFilterableGrid(dataProvider, gridSettings.get()),
    gridSettings,
    initializer
)

fun <T : Any, F : Any, S : Any, C : FilterableSortableLocalizedColumn<T, F, S>, G : LayoutPersistingFilterableGrid<T, F, S, C>> AcrariumGridView<T, F, S, C, G>.grid(initializer: G.() -> Unit) {
    grid.apply(initializer)
    updateHeader()
}
