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
import com.github.appreciated.apexcharts.config.chart.Type

/**
 * @author lukas
 * @since 01.06.18
 */
internal class PieChart(captionId: String, vararg params: Any) : Chart<String>(captionId, *params) {

    override fun createChart(map: Map<String, Long>): ApexCharts {
        val list = map.entries.map { it.key to it.value }.sortedByDescending { it.second }.toMutableList()
        if (list.size > MAX_PARTS) {
            val replace = list.subList(MAX_PARTS, list.size)
            val other = getTranslation(Messages.OTHER) to  replace.map { it.second }.sum()
            replace.clear()
            list.add(other)
        }
        return ApexChartsBuilder.get()
            .withChart(ChartBuilder.get().withType(Type.PIE).withBackground("transparent").build())
                .withLabels(*list.map { it.first }.toTypedArray())
                .withSeries(*list.map {it.second.toDouble() }.toTypedArray())
                .build()
    }

    companion object {
        private const val MAX_PARTS = 4
    }
}