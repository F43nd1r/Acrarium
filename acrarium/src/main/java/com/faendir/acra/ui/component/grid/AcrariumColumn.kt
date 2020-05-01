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

import com.faendir.acra.dataprovider.QueryDslFilter
import com.faendir.acra.dataprovider.QueryDslFilterWithParameter
import com.faendir.acra.dataprovider.QueryDslSortOrder
import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.getConverter
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.core.types.dsl.StringExpression
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import java.util.stream.Stream

class AcrariumColumn<T>(private val acrariumGrid: AcrariumGrid<T>, columnId: String, renderer: Renderer<T>) : Grid.Column<T>(acrariumGrid, columnId, renderer), LocaleChangeObserver {
    private var caption: Pair<String, Array<out Any>>? = null
    internal var filter: QueryDslFilter? = null

    init {
        isResizable = true
        isAutoWidth = true
        flexGrow = 0
    }

    fun setCaption(id: String, vararg params: Any): AcrariumColumn<T> {
        caption = id to params
        return this
    }

    fun setSortable(sort: Expression<out Comparable<*>>): AcrariumColumn<T> {
        setSortOrderProvider { direction: SortDirection -> Stream.of(QueryDslSortOrder(sort, direction)) }
        isSortable = true
        return this
    }

    fun setSortableAndFilterable(expr: StringExpression): AcrariumColumn<T> {
        return setSortable(expr).setFilterable(Translatable.createTextFieldWithHint(Messages.FILTER), object : QueryDslFilterWithParameter<String?> {
            override var parameter: String? = null

            override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = parameter?.let { query.where(expr.contains(it)) } ?: query

        })
    }

    inline fun <reified U> setSortableAndFilterable(expr: NumberExpression<U>): AcrariumColumn<T> where U : Number, U : Comparable<*> {
        val converter = getConverter<U>()
        return setSortable(expr).setFilterable(Translatable.createNumberFieldWithHint(Messages.FILTER), object : QueryDslFilterWithParameter<Double?> {
            override var parameter: Double? = null

            override fun <T> apply(query: JPAQuery<T>): JPAQuery<T> = parameter?.let {query.where(expr.eq(converter(it))) } ?: query

        })
    }

    fun <C, U> setFilterable(filterField: C, filter: QueryDslFilterWithParameter<U>): AcrariumColumn<T> where C : Component, C : HasValue<out HasValue.ValueChangeEvent<U>, U>, C: HasSize {
        filterField.setWidthFull()
        acrariumGrid.filterRow.getCell(this).setComponent(filterField)
        this.filter = filter
        filterField.addValueChangeListener { event ->
            filter.parameter = event.value
            acrariumGrid.dataProvider.refreshAll()
        }
        return this
    }

    override fun localeChange(event: LocaleChangeEvent?) {
        caption?.let { setHeader(getTranslation(it.first, *it.second)) }
    }
}