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

package com.faendir.acra.ui.component.statistics;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.util.LocalSettings;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lukas
 * @since 01.06.18
 */
class PieChart extends Chart<String> {
    private static final int MAX_PARTS = 4;
    PieChart(@NonNull LocalSettings localSettings, @NonNull String captionId, @NonNull Object... params) {
        super(localSettings, captionId, params);
    }

    @Override
    public ApexCharts createChart(@NonNull Map<String, Long> map) {
        List<Pair<String, Long>> list = map.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .sorted(Comparator.<Pair<String, Long>, Long>comparing(Pair::getSecond).reversed())
                .collect(Collectors.toList());
        if(list.size() > MAX_PARTS) {
            List<Pair<String, Long>> replace = list.subList(MAX_PARTS, list.size());
            Pair<String, Long> other = Pair.of(getTranslation(Messages.OTHER), replace.stream().mapToLong(Pair::getSecond).sum());
            replace.clear();
            list.add(other);
        }
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.pie).withBackground("transparent").build())
                .withLabels(list.stream().map(Pair::getFirst).toArray(String[]::new))
                .withSeries(list.stream().map(p -> p.getSecond().doubleValue()).toArray(Double[]::new))
                .build();
    }
}
