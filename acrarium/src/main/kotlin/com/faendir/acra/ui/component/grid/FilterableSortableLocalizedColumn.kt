package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.data.value.ValueChangeMode
import java.util.stream.Stream

open class FilterableSortableLocalizedColumn<T : Any, F: Any, S: Any>(grid: Grid<T>, renderer: Renderer<T>, columnId: String) : LocalizedColumn<T>(grid, renderer, columnId) {
    var filter: (() -> F?)? = null
        private set
    var filterComponent: Component? = null
        private set

    fun setSortable(sort: S) {
        setSortOrderProvider { direction: SortDirection -> Stream.of(AcrariumSort(sort, direction)) }
        isSortable = true
    }

    fun setFilterable(createFilter: (String) -> F, captionId: String, vararg params: Any) {
        setFilterable(Translatable.createTextFieldWithHint(captionId, *params).with {
            valueChangeMode = ValueChangeMode.EAGER
        }, null) { parameter -> parameter?.let { createFilter(it) } }
    }

    fun setFilterable(filter: F, default: Boolean, captionId: String, vararg params: Any) {
        return setFilterable(Translatable.createCheckbox(captionId, *params).with { value = default }, default) { if (it) filter else null }
    }

    private fun <C, U> setFilterable(
        filterField: C,
        default: U,
        filter: (U) -> F?,
    ) where C : Component, C : HasValue<out HasValue.ValueChangeEvent<U>, U>, C : HasSize {
        filterField.setWidthFull()
        var currentValue: U = default
        this.filter = { filter(currentValue) }
        this.filterComponent = filterField
        filterField.addValueChangeListener { event ->
            currentValue = event.value
            grid.dataProvider.refreshAll()
        }
    }
}