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
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.function.ValueProvider
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.RouteConfiguration
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * @author lukas
 * @since 13.07.18
 */
class AcrariumGrid<T>(val dataProvider: QueryDslDataProvider<T>) : Grid<T>() {
    internal val filterRow by lazy { appendHeaderRow() }
    val acrariumColumns: List<AcrariumColumn<T>>
        get() = super.getColumns().filterIsInstance<AcrariumColumn<T>>()

    init {
        dataCommunicator.setDataProvider(dataProvider, object : QueryDslFilter {
            override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = acrariumColumns.mapNotNull { it.filter }.fold(query) { q, f -> f.apply(q) }
        })
        setSizeFull()
        isMultiSort = true
        isColumnReorderingAllowed = true
        //create default header row
        appendHeaderRow()
    }

    override fun getDefaultColumnFactory(): BiFunction<Renderer<T>, String, Column<T>> = BiFunction { renderer, columnId -> AcrariumColumn(this, columnId, renderer) }

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

    fun <C, R> addOnClickNavigation(target: Class<C>, transform: (T) -> R) where C : Component, C : HasUrlParameter<R> {
        addItemClickListener { e: ItemClickEvent<T> ->
            ui.ifPresent(if (e.button == 1 || e.isCtrlKey) Consumer {
                it.page.executeJs("""window.open("${RouteConfiguration.forSessionScope().getUrl(target, transform(e.item))}", "blank", "");""")
            } else Consumer { it.navigate(target, transform(e.item)) })
        }
    }
}