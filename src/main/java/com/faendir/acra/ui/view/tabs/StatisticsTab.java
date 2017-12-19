package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.util.Style;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.addon.JFreeChartWrapper;
import org.vaadin.risto.stepper.IntStepper;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Lukas
 * @since 22.05.2017
 */
@SpringComponent
@ViewScope
public class StatisticsTab implements MyTabSheet.Tab {
    public static final String CAPTION = "Statistics";
    private static final Color BACKGROUND_GRAY = new Color(0xfafafa); //vaadin gray
    private static final Color BLUE = new Color(0x197de1); //vaadin blue
    @NonNull private final ReportRepository reportRepository;

    @Autowired
    public StatisticsTab(@NonNull ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        IntStepper numberField = new IntStepper("Days");
        numberField.setValue(30);
        numberField.setMinValue(5);
        Panel timePanel = new Panel();
        timePanel.setSizeUndefined();
        Style.apply(timePanel, Style.NO_BACKGROUND, Style.NO_BORDER);
        numberField.addValueChangeListener(e -> createTimeChart(e.getValue(), app, timePanel));
        VerticalLayout timeLayout = new VerticalLayout(numberField, timePanel);
        timeLayout.setComponentAlignment(numberField, Alignment.MIDDLE_RIGHT);
        timeLayout.setSizeUndefined();
        Style.NO_PADDING.apply(timeLayout);
        Panel versionPanel = new Panel();
        versionPanel.setSizeUndefined();
        Style.apply(versionPanel, Style.NO_BACKGROUND, Style.NO_BORDER, Style.PADDING_TOP);
        createTimeChart(30, app, timePanel);
        createVersionChart(app, versionPanel);
        Panel root = new Panel((new CssLayout(timeLayout, versionPanel)));
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        return root;
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    private void createTimeChart(int age, @NonNull App app, @NonNull Panel panel) {
        TimeSeries series = new TimeSeries("Date");
        series.setMaximumItemAge(age);
        series.add(new Day(new Date()), 0);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -age);
        series.add(new Day(start.getTime()), 0);
        reportRepository.countAllByDayAfter(app, start.getTime()).forEach(dayCount -> series.addOrUpdate(new Day(dayCount.getGroup()), dayCount.getCount()));
        JFreeChart chart = ChartFactory.createXYBarChart("", "Date", true, "Reports", new TimeSeriesCollection(series), PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));
        plot.setBackgroundPaint(BACKGROUND_GRAY);
        chart.setBackgroundPaint(BACKGROUND_GRAY);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setOutlineVisible(false);
        XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, BLUE);
        barRenderer.setBarAlignmentFactor(0.5);
        barRenderer.setMargin(0.2);
        panel.setContent(new JFreeChartWrapper(chart));
    }

    private void createVersionChart(@NonNull App app, @NonNull Panel panel) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        reportRepository.countAllByAndroidVersion(app).forEach(pair -> dataset.insertValue(0, pair.getGroup(), pair.getCount()));
        dataset.sortByKeys(SortOrder.ASCENDING);
        JFreeChart chart = ChartFactory.createPieChart("Reports per Android Version", dataset, false, false, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setBackgroundPaint(BACKGROUND_GRAY);
        chart.setBackgroundPaint(BACKGROUND_GRAY);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(BACKGROUND_GRAY);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
        //noinspection unchecked
        ((List<String>) dataset.getKeys()).forEach(key -> plot.setExplodePercent(key, 0.01));
        panel.setContent(new JFreeChartWrapper(chart));
    }
}
