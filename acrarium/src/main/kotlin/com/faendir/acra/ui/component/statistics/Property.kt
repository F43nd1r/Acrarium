/*
 * (C) Copyright 2018-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.statistics

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionKey
import com.faendir.acra.persistence.version.VersionName
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.persistence.version.toVersionKey
import com.faendir.acra.ui.component.CssGridLayout
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.preventWhiteSpaceBreaking
import com.vaadin.flow.component.*
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.NumberField
import org.jooq.Condition
import org.jooq.Field
import org.jooq.SelectField
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * @author lukas
 * @since 01.06.18
 */
internal class Property<F, C, T, E : HasValue.ValueChangeEvent<F>, V> private
constructor(
    private val appId: AppId,
    private val filterComponent: C,
    private val filter: (F) -> Condition,
    private val chart: Chart<T>,
    private val reportRepository: ReportRepository,
    private val select: SelectField<V?>,
    private val getChartValue: (V) -> T,
    filterTextId: String,
    vararg params: Any
)
        where C : Component, C : HasValue<E, F>, C : HasEnabled, C : HasSize, C : HasStyle {
    private val checkBox = Translatable.createCheckbox(filterTextId, *params).with { preventWhiteSpaceBreaking() }

    init {
        filterComponent.isEnabled = false
        filterComponent.setWidthFull()
        checkBox.addValueChangeListener { filterComponent.isEnabled = it.value }
    }

    fun addTo(filterLayout: CssGridLayout, chartLayout: FlexComponent) {
        filterLayout.add(checkBox, filterComponent)
        chartLayout.add(chart)
        chartLayout.expand(chart)
    }

    fun applyFilter(expression: Condition?): Condition? {
        return if (checkBox.content.value && filterComponent.value != null) {
            filter(filterComponent.value).and(expression)
        } else expression
    }

    fun update(expression: Condition?) {
        chart.setContent(reportRepository.countGroupedBy(appId, select, expression).filterKeys { it != null }.mapKeys { getChartValue(it.key!!) })
    }

    internal class Factory(
        private val reportRepository: ReportRepository,
        private val versionRepository: VersionRepository,
        private val expression: Condition?,
        private val appId: AppId
    ) {
        fun createStringProperty(field: Field<String?>, filterTextId: String, chartTitleId: String): Property<*, *, *, *, *> {
            val list = reportRepository.get(appId, field, where = field.isNotNull.and(expression))
            val select = Select<String>()
            select.setItems(list)
            if (list.isNotEmpty()) {
                select.value = list[0]
            }
            return Property(appId, select, { field.eq(it) }, PieChart(chartTitleId), reportRepository, field, { it }, filterTextId)
        }

        fun createVersionProperty(field: Field<VersionKey>, filterTextId: String, chartTitleId: String): Property<*, *, *, *, *> {
            val list = reportRepository.get(appId, field, where = expression)
            val versionNames = versionRepository.getVersionNames(appId)
            val select = Select<VersionName>()
            select.setItems(versionNames)
            select.setItemLabelGenerator { it.name }
            if (list.isNotEmpty()) {
                select.value = versionNames.find { list[0].code == it.code && list[0].flavor == it.flavor }
            }
            return Property(
                appId,
                select,
                { field.eq(it.toVersionKey()) },
                PieChart(chartTitleId),
                reportRepository,
                field,
                { (code, flavor) -> versionNames.first { code == it.code && flavor == it.flavor }.name },
                filterTextId
            )
        }

        fun createAgeProperty(field: Field<Instant?>, filterTextId: String, chartTitleId: String): Property<*, *, *, *, *> {
            val stepper = NumberField()
            stepper.value = 30.0
            return Property(
                appId, stepper, { field.greaterThan(Instant.now().minus(it.toInt().toLong(), ChronoUnit.DAYS)) }, TimeChart(chartTitleId),
                reportRepository, field, { it }, filterTextId
            )
        }
    }
}