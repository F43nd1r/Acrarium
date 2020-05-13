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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.QReport
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setWidth
import com.faendir.acra.util.LocalSettings
import com.querydsl.core.types.dsl.BooleanExpression
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.textfield.NumberField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author lukas
 * @since 21.05.18
 */
class Statistics(app: App, private val baseExpression: BooleanExpression?, dataService: DataService, localSettings: LocalSettings) : Composite<FlexLayout>() {
    private val properties: MutableList<Property<*, *, *, *>> = mutableListOf()

    init {
        val filterLayout = FormLayout()
        filterLayout.setResponsiveSteps(ResponsiveStep("0px", 1))
        filterLayout.setWidthFull()
        val card = Card(filterLayout)
        card.setHeader(Translatable.createLabel(Messages.FILTER))
        card.setWidth(500, Unit.PIXEL)
        val dayStepper = NumberField()
        dayStepper.value = 30.0
        val factory = Property.Factory(dataService, baseExpression, localSettings, app)
        properties.add(factory.createAgeProperty(QReport.report.date, Messages.LAST_X_DAYS, Messages.REPORTS_OVER_TIME))
        properties.add(factory.createStringProperty(QReport.report.androidVersion, Messages.ANDROID_VERSION, Messages.REPORTS_PER_ANDROID_VERSION))
        properties.add(factory.createStringProperty(QReport.report.stacktrace.version.name, Messages.APP_VERSION, Messages.REPORTS_PER_APP_VERSION))
        properties.add(factory.createStringProperty(QReport.report.phoneModel, Messages.PHONE_MODEL, Messages.REPORTS_PER_PHONE_MODEL))
        properties.add(factory.createStringProperty(QReport.report.brand, Messages.PHONE_BRAND, Messages.REPORTS_PER_BRAND))
        content.flexWrap = FlexLayout.FlexWrap.WRAP
        content.setWidthFull()
        content.removeAll()
        content.add(card)
        content.expand(card)
        properties.forEach { it.addTo(filterLayout, content) }
        filterLayout.add(Translatable.createButton(Messages.APPLY) { update() })
        GlobalScope.launch {
            delay(100)
            ui.ifPresent { it.access { update() } }
        }
    }

    private fun update() {
        val expression = properties.fold(baseExpression) { expr, property -> property.applyFilter(expr) }
        properties.forEach { it.update(expression) }
    }

}