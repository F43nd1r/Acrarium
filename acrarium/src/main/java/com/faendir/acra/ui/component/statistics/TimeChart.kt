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
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.SelectionBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lukas
 * @since 01.06.18
 */
class TimeChart extends Chart<Date> {
    TimeChart(@NonNull LocalSettings localSettings, @NonNull String captionId, @NonNull Object... params) {
        super(localSettings, captionId, params);
    }

    @Override
    public ApexCharts createChart(@NonNull Map<Date, Long> map) {
        List<Pair<Date, Long>> list = map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).sorted(Comparator.comparing(Pair::getFirst)).collect(Collectors.toList());
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.bar)
                        .withBackground("transparent")
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withSelection(SelectionBuilder.get().withEnabled(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withXaxis(XAxisBuilder.get().withType(XAxisType.datetime).build())
                .withSeries(new Series<>(getTranslation(Messages.REPORTS), list.stream().map(p -> new Object[]{p.getFirst(), p.getSecond()}).toArray(Object[][]::new)))
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withLabels(getTranslation(Messages.REPORTS))
                .build();
    }
}
