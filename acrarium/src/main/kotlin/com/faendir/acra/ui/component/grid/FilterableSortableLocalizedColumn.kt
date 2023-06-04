/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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

    fun setFilterableContains(createFilter: (String) -> F, captionId: String, vararg params: Any) {
        setFilterable(Translatable.createTextFieldWithHint(captionId, *params).with {
            valueChangeMode = ValueChangeMode.EAGER
        }, null) { parameter -> parameter?.let { createFilter(it) } }
    }

    fun setFilterableToggle(filter: F, default: Boolean, captionId: String, vararg params: Any) {
        setFilterable(Translatable.createCheckbox(captionId, *params).with { value = default }, default) { if (it) filter else null }
    }

    fun <U> setFilterableIs(options: List<U>, getLabel: (U) -> String, createFilter: (U & Any) -> F, captionId: String, vararg params: Any) {
        setFilterable(Translatable.createSelect(options, getLabel, captionId, params), null) { parameter -> parameter?.let { createFilter(it) } }
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