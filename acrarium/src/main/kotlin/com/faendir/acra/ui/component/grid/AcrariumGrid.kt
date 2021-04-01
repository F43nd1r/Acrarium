/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.dataprovider.QueryDslFilter
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.util.PARAM
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.function.ValueProvider
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * @author lukas
 * @since 13.07.18
 */
class AcrariumGrid<T>(val dataProvider: QueryDslDataProvider<T>, var gridSettings: GridSettings? = null) : Grid<T>() {
    val acrariumColumns: List<AcrariumColumn<T>>
        get() = super.getColumns().filterIsInstance<AcrariumColumn<T>>()

    init {
        dataCommunicator.setDataProvider(dataProvider, object : QueryDslFilter {
            override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = acrariumColumns.mapNotNull { it.filter }.fold(query) { q, f -> f.apply(q) }
        })
        setSizeFull()
        isMultiSort = true
        isColumnReorderingAllowed = true
    }

    override fun getDefaultColumnFactory(): BiFunction<Renderer<T>, String, Column<T>> =
        BiFunction { renderer, columnId -> AcrariumColumn(this, columnId, renderer) }

    override fun addColumn(propertyName: String): AcrariumColumn<T> = super.addColumn(propertyName) as AcrariumColumn<T>

    override fun addColumn(valueProvider: ValueProvider<T, *>): AcrariumColumn<T> = super.addColumn(valueProvider) as AcrariumColumn<T>

    override fun addColumn(renderer: Renderer<T>): AcrariumColumn<T> {
        return super.addColumn(renderer) as AcrariumColumn<T>
    }

    @Deprecated(message = "Not supported")
    override fun <V : Comparable<V>?> addColumn(valueProvider: ValueProvider<T, V>, vararg sortingProperties: String): Column<T> {
        throw UnsupportedOperationException()
    }

    @Deprecated(message = "Not supported")
    override fun addColumn(renderer: Renderer<T>, vararg sortingProperties: String): Column<T> {
        throw UnsupportedOperationException()
    }

    /**
     * workaround https://github.com/vaadin/vaadin-grid/issues/1864
     */
    override fun recalculateColumnWidths() {
        getElement().executeJs("setTimeout(() => { this.recalculateColumnWidths() }, 10)");
    }

    fun <C, R> addOnClickNavigation(target: Class<C>, transform: (T) -> R) where C : Component {
        addItemClickListener { e: ItemClickEvent<T> ->
            ui.ifPresent(if (e.button == 1 || e.isCtrlKey) Consumer {
                it.page.executeJs(
                    """window.open("${
                        RouteConfiguration.forSessionScope().getUrl(target, RouteParameters(PARAM, transform(e.item).toString()))
                    }", "blank", "");"""
                )
            } else Consumer { it.navigate(target, RouteParameters(PARAM, transform(e.item).toString())) })
        }
    }

    /**
     * call when all columns were added
     */
    fun loadLayout() {
        gridSettings?.apply {
            val orderedColumns = columnOrder.mapNotNull { key -> acrariumColumns.find { it.key == key } }
            val unorderedColumns = acrariumColumns - orderedColumns
            setColumnOrder(orderedColumns + unorderedColumns)
            hiddenColumns.forEach { key -> acrariumColumns.find { it.key == key }?.isVisible = false }
        }
    }

    fun addOnLayoutChangedListener(listener: (gridSettings: GridSettings) -> Unit) {
        addColumnReorderListener { event ->
            val settings = GridSettings(event.columns.mapNotNull { it.key }, gridSettings?.hiddenColumns ?: emptyList())
            gridSettings = settings
            listener(settings)
        }
        acrariumColumns.forEach { column ->
            column.addVisibilityChangeListener {
                val settings = GridSettings(gridSettings?.columnOrder ?: columns.mapNotNull { it.key }, columns.filter { !it.isVisible }.mapNotNull { it.key })
                gridSettings = settings
                listener(settings)
            }
        }
    }
}