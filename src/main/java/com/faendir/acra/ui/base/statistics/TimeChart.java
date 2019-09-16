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
package com.faendir.acra.ui.base.statistics;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.util.LocalSettings;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
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
        ApexCharts chart = new ApexCharts()
                .withChart(ChartBuilder.get().withType(Type.bar).withBackground("transparent").build())
                .withXaxis(XAxisBuilder.get().withType(XAxisType.datetime).build())
                .withSeries(new Series<>(list.stream().map(p -> new Object[]{p.getFirst(), p.getSecond()}).toArray(Object[][]::new)))
                /*.withTheme(ThemeBuilder.get()
                        .withMode(isDarkTheme() ? Mode.dark : Mode.light)
                        .withPalette("palette1")
                        .withMonochrome(MonochromeBuilder.get().withColor("0x197de1").withEnabled(true).withShadeIntensity(0.1).withShadeTo(ShadeTo.light).build())
                        .build())*/
                .withLabels(getTranslation(Messages.REPORTS));
        /*TimeSeries series = new TimeSeries("Date");
        series.add(new Day(new Date()), 0);
        map.forEach((date, count) -> series.addOrUpdate(new Day(date), count));
        JFreeChart chart = ChartFactory.createXYBarChart("",
                "Date",
                true,
                "Reports",
                new TimeSeriesCollection(series),
                PlotOrientation.VERTICAL,
                false,
                false,
                false);
        chart.setBackgroundPaint(null);
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));
        plot.setBackgroundAlpha(0);
        plot.setDomainGridlinesVisible(false);
        plot.setOutlineVisible(false);
        Paint foregroundColor = getForegroundColor();
        plot.setRangeGridlinePaint(foregroundColor);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelPaint(foregroundColor);
        domainAxis.setLabelFont(Statistics.LABEL_FONT);
        domainAxis.setTickLabelFont(Statistics.LABEL_FONT);
        domainAxis.setTickLabelPaint(foregroundColor);
        domainAxis.setAxisLinePaint(foregroundColor);
        domainAxis.setTickMarkPaint(foregroundColor);
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelPaint(foregroundColor);
        rangeAxis.setLabelFont(Statistics.LABEL_FONT);
        rangeAxis.setTickLabelFont(Statistics.LABEL_FONT);
        rangeAxis.setTickLabelPaint(foregroundColor);
        rangeAxis.setAxisLinePaint(foregroundColor);
        rangeAxis.setTickMarkPaint(foregroundColor);
        XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, Statistics.BLUE);
        barRenderer.setBarAlignmentFactor(0.5);
        barRenderer.setMargin(0.2);*/
        return chart;
    }
}
