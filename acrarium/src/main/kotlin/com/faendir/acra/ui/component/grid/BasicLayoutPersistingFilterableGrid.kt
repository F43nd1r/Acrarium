package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.settings.GridSettings
import com.vaadin.flow.data.renderer.Renderer

class BasicLayoutPersistingFilterableGrid<T : Any, F : Any, S : Any>(dataProvider: AcrariumDataProvider<T, F, S>, gridSettings: GridSettings?) :
    LayoutPersistingFilterableGrid<T, F, S, FilterableSortableLocalizedColumn<T, F, S>>(dataProvider, gridSettings) {
    override val columnFactory: (Renderer<T>, String) -> FilterableSortableLocalizedColumn<T, F, S> = { renderer, id -> FilterableSortableLocalizedColumn(this, renderer, id) }
}