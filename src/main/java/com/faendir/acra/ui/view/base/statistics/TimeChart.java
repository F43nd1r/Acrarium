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

package com.faendir.acra.ui.view.base.statistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.lang.NonNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
class TimeChart extends Chart<LocalDateTime> {
    TimeChart(@NonNull String caption) {
        super(caption);
    }

    @Override
    public JFreeChart createChart(@NonNull Map<LocalDateTime, Long> map) {
        TimeSeries series = new TimeSeries("Date");
        series.add(new Day(new Date()), 0);
        map.forEach((date, count) -> series.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), count));
        JFreeChart chart = ChartFactory.createXYBarChart("", "Date", true, "Reports", new TimeSeriesCollection(series), PlotOrientation.VERTICAL, false, false, false);
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
        domainAxis.setTickLabelPaint(foregroundColor);
        domainAxis.setAxisLinePaint(foregroundColor);
        domainAxis.setTickMarkPaint(foregroundColor);
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelPaint(foregroundColor);
        rangeAxis.setTickLabelPaint(foregroundColor);
        rangeAxis.setAxisLinePaint(foregroundColor);
        rangeAxis.setTickMarkPaint(foregroundColor);
        XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, Statistics.BLUE);
        barRenderer.setBarAlignmentFactor(0.5);
        barRenderer.setMargin(0.2);
        return chart;
    }
}
