/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.statistics

import com.faendir.acra.model.App
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.preventWhiteSpaceBreaking
import com.faendir.acra.util.LocalSettings
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.core.types.dsl.DateTimePath
import com.querydsl.sql.SQLExpressions
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasEnabled
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.NumberField
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * @author lukas
 * @since 01.06.18
 */
internal class Property<F, C, T, E : HasValue.ValueChangeEvent<F>> private
constructor(private val app: App, private val filterComponent: C, private val filter: (F) -> BooleanExpression, chart: Chart<T>,
            private val dataService: DataService, select: Expression<T>, filterTextId: String, vararg params: Any)
        where C : Component, C : HasValue<E, F>, C : HasEnabled, C : HasSize, C : HasStyle {
    private val checkBox: Translatable<Checkbox>
    private val chart: Chart<T>
    private val select: Expression<T>

    init {
        checkBox = Translatable.createCheckbox(filterTextId, *params)
        checkBox.preventWhiteSpaceBreaking()
        this.chart = chart
        this.select = select
        filterComponent.isEnabled = false
        filterComponent.setWidthFull()
        checkBox.content.addValueChangeListener { filterComponent.isEnabled = it.value }
    }

    fun addTo(filterLayout: FormLayout, chartLayout: FlexComponent) {
        filterLayout.addFormItem(filterComponent, checkBox)
        chartLayout.add(chart)
        chartLayout.expand(chart)
    }

    fun applyFilter(expression: BooleanExpression?): BooleanExpression? {
        return if (checkBox.content.value && filterComponent.value != null) {
            filter(filterComponent.value).and(expression)
        } else expression
    }

    fun update(expression: BooleanExpression?) {
        chart.setContent(dataService.countReports(app, expression, select))
    }

    internal class Factory(private val dataService: DataService, private val expression: BooleanExpression?, private val localSettings: LocalSettings, private val app: App) {
        fun createStringProperty(stringExpression: ComparableExpressionBase<String>, filterTextId: String, chartTitleId: String): Property<*, *, *, *> {
            val list = dataService.getFromReports(app, expression, stringExpression)
            val select = Select(*list.toTypedArray())
            if (list.isNotEmpty()) {
                select.value = list[0]
            }
            return Property(app, select, { stringExpression.eq(it) }, PieChart(chartTitleId), dataService, stringExpression, filterTextId)
        }

        fun createAgeProperty(dateTimeExpression: DateTimePath<ZonedDateTime>, filterTextId: String, chartTitleId: String): Property<*, *, *, *> {
            val stepper = NumberField()
            stepper.value = 30.0
            return Property(app, stepper, { dateTimeExpression.after(ZonedDateTime.now().minus(it.toInt().toLong(), ChronoUnit.DAYS)) }, TimeChart(chartTitleId),
                    dataService, SQLExpressions.date(Date::class.java, dateTimeExpression), filterTextId)
        }

    }
}