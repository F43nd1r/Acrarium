/*
 * (C) Copyright 2020-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.NOT_NULL
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.CssGridLayout
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Align
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.setAlignItems
import com.faendir.acra.ui.ext.setWidth
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.textfield.NumberField
import org.jooq.Condition

/**
 * @author lukas
 * @since 21.05.18
 */
open class Statistics(appId: AppId, private val baseExpression: Condition?, reportRepository: ReportRepository, versionRepository: VersionRepository) : Composite<FlexLayout>() {
    private val properties: MutableList<Property<*, *, *, *, *>> = mutableListOf()

    init {
        val filterLayout = CssGridLayout()
        filterLayout.setWidthFull()
        filterLayout.setTemplateColumns("auto auto")
        filterLayout.setRowGap(0.5, SizeUnit.EM)
        filterLayout.setAlignItems(Align.CENTER)
        val card = Card(filterLayout)
        card.setHeader(Translatable.createLabel(Messages.FILTER))
        card.setWidth(500, SizeUnit.PIXEL)
        val dayStepper = NumberField()
        dayStepper.value = 30.0
        val factory = Property.Factory(reportRepository, versionRepository, baseExpression, appId)
        properties.add(factory.createAgeProperty(REPORT.DATE.NOT_NULL, Messages.LAST_X_DAYS, Messages.REPORTS_OVER_TIME))
        properties.add(factory.createStringProperty(REPORT.ANDROID_VERSION.NOT_NULL, Messages.ANDROID_VERSION, Messages.REPORTS_PER_ANDROID_VERSION))
        properties.add(factory.createVersionProperty(REPORT.VERSION_KEY, Messages.APP_VERSION, Messages.REPORTS_PER_APP_VERSION))
        properties.add(factory.createStringProperty(REPORT.PHONE_MODEL.NOT_NULL, Messages.PHONE_MODEL, Messages.REPORTS_PER_PHONE_MODEL))
        properties.add(factory.createStringProperty(REPORT.BRAND.NOT_NULL, Messages.PHONE_BRAND, Messages.REPORTS_PER_BRAND))
        content.flexWrap = FlexLayout.FlexWrap.WRAP
        content.setWidthFull()
        content.removeAll()
        content.add(card)
        content.expand(card)
        properties.forEach { it.addTo(filterLayout, content) }
        filterLayout.addSpanning(Translatable.createButton(Messages.APPLY) { update() }, 2)
        update()
    }

    private fun update() {
        val expression = properties.fold(baseExpression) { expr, property -> property.applyFilter(expr) }
        properties.forEach { it.update(expression) }
    }

}