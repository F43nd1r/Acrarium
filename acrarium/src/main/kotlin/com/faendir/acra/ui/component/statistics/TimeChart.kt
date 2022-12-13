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
import com.github.appreciated.apexcharts.ApexCharts
import com.github.appreciated.apexcharts.ApexChartsBuilder
import com.github.appreciated.apexcharts.config.builder.ChartBuilder
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder
import com.github.appreciated.apexcharts.config.chart.Type
import com.github.appreciated.apexcharts.config.chart.builder.SelectionBuilder
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder
import com.github.appreciated.apexcharts.config.xaxis.XAxisType
import com.github.appreciated.apexcharts.helper.Series
import java.time.Instant
import java.util.*

/**
 * @author lukas
 * @since 01.06.18
 */
internal class TimeChart(captionId: String, vararg params: Any) : Chart<Instant>(captionId, *params) {
    override fun createChart(map: Map<Instant, Int>): ApexCharts {
        val list = map.asSequence().sortedBy { it.key }.map { arrayOf<Any>(Date.from(it.key), it.value) }.toList()
        return ApexChartsBuilder.get()
            .withChart(
                ChartBuilder.get()
                    .withType(Type.BAR)
                    .withBackground("transparent")
                    .withToolbar(ToolbarBuilder.get().withShow(false).build())
                    .withSelection(SelectionBuilder.get().withEnabled(false).build())
                    .withZoom(ZoomBuilder.get().withEnabled(false).build())
                    .build()
            )
            .withXaxis(XAxisBuilder.get().withType(XAxisType.DATETIME).build())
                .withSeries(Series(getTranslation(Messages.REPORTS), *list.toTypedArray()))
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withLabels(getTranslation(Messages.REPORTS))
                .build()
    }
}