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

import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setMaxWidthFull
import com.faendir.acra.ui.ext.setWidth
import com.github.appreciated.apexcharts.ApexCharts
import com.vaadin.flow.component.Composite

/**
 * @author lukas
 * @since 01.06.18
 */
internal abstract class Chart<T>(captionId: String, vararg params: Any) : Composite<Card>() {

    init {
        content.setWidth(500, Unit.PIXEL)
        content.setMaxWidthFull()
        content.setHeader(Translatable.createLabel(captionId, *params))
    }

    fun setContent(map: Map<T, Long>) {
        content.removeContent()
        content.add(createChart(map))
    }

    protected abstract fun createChart(map: Map<T, Long>): ApexCharts
}