package com.faendir.acra.ui.view.base.statistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.lang.NonNull;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
class PieChart extends Chart<String> {
    PieChart(@NonNull String caption) {
        super(caption);
    }

    @Override
    public JFreeChart createChart(@NonNull Map<String, Long> map) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        map.forEach((label, count) -> dataset.insertValue(0, label, count));
        dataset.sortByKeys(SortOrder.ASCENDING);
        JFreeChart chart = ChartFactory.createPieChart("", dataset, false, false, false);
        chart.setBackgroundPaint(null);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setBackgroundAlpha(0);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        Paint foregroundColor = getForegroundColor();
        plot.setLabelPaint(foregroundColor);
        plot.setLabelLinkPaint(foregroundColor);
        plot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
        //noinspection unchecked
        ((List<String>) dataset.getKeys()).forEach(key -> plot.setExplodePercent(key, 0.01));
        return chart;
    }
}
