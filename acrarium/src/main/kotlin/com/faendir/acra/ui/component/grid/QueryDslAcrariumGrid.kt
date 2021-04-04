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
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.data.renderer.Renderer

/**
 * @author lukas
 * @since 13.07.18
 */
class QueryDslAcrariumGrid<T>(val dataProvider: QueryDslDataProvider<T>, var gridSettings: GridSettings? = null) :
    AbstractAcrariumGrid<T, QueryDslAcrariumColumn<T>>() {
    override val columnFactory: (Renderer<T>, String) -> QueryDslAcrariumColumn<T> = { renderer, id -> QueryDslAcrariumColumn(this, renderer, id) }
    val acrariumColumns: List<QueryDslAcrariumColumn<T>>
        get() = super.getColumns().filterIsInstance<QueryDslAcrariumColumn<T>>()

    init {
        dataCommunicator.setDataProvider(dataProvider, object : QueryDslFilter {
            override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = acrariumColumns.mapNotNull { it.filter }.fold(query) { q, f -> f.apply(q) }
        })
        setSizeFull()
        isMultiSort = true
        isColumnReorderingAllowed = true
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