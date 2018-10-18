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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.lang.NonNull;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
class PieChart extends Chart<String> {
    private static final int MAX_PARTS = 4;
    PieChart(String caption) {
        super(caption);
    }

    @Override
    public JFreeChart createChart(@NonNull Map<String, Long> map) {
        List<Map.Entry<String, Long>> values = new ArrayList<>(map.entrySet());
        values.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
        DefaultPieDataset dataset = new DefaultPieDataset();
        values.subList(0, Math.min(MAX_PARTS, values.size())).forEach(e -> dataset.insertValue(0, e.getKey(), e.getValue()));
        dataset.sortByValues(SortOrder.DESCENDING);
        if (values.size() > MAX_PARTS) {
            dataset.insertValue(dataset.getItemCount(), "Other", values.subList(MAX_PARTS, values.size()).stream().mapToLong(Map.Entry::getValue).sum());
        }
        JFreeChart chart = ChartFactory.createPieChart("", dataset, false, false, false);
        chart.setBackgroundPaint(null);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setStartAngle(0);
        plot.setShadowPaint(null);
        plot.setBackgroundAlpha(0);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        Paint foregroundColor = getForegroundColor();
        plot.setLabelPaint(foregroundColor);
        plot.setLabelFont(Statistics.LABEL_FONT);
        plot.setLabelLinkPaint(foregroundColor);
        plot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", NumberFormat.getNumberInstance(), new DecimalFormat("0.0%")));
        //noinspection unchecked
        ((List<String>) dataset.getKeys()).forEach(key -> plot.setExplodePercent(key, 0.01));
        return chart;
    }
}
