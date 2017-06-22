package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.ReportInfo;
import com.faendir.acra.util.Style;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import org.jetbrains.annotations.NotNull;
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
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.util.SortOrder;
import org.vaadin.addon.JFreeChartWrapper;
import org.vaadin.risto.stepper.IntStepper;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 22.05.2017
 */
public class StatisticsTab extends HorizontalLayout implements DataManager.Listener<ReportInfo> {
    public static final String CAPTION = "Statistics";
    private static final Color BACKGROUND_GRAY = new Color(0xfafafa); //vaadin gray
    private static final Color BLUE = new Color(0x197de1); //vaadin blue
    @NotNull private final VerticalLayout timeLayout;
    @NotNull private final IntStepper numberField;
    @NotNull private final String app;
    @NotNull private final DataManager dataManager;
    @NotNull private JFreeChartWrapper timeChart;
    @NotNull private JFreeChartWrapper versionChart;

    public StatisticsTab(@NotNull String app, @NotNull DataManager dataManager) {
        this.app = app;
        this.dataManager = dataManager;
        setCaption(CAPTION);
        numberField = new IntStepper("Days");
        numberField.setValue(30);
        numberField.setMinValue(5);
        numberField.addValueChangeListener(e -> timeChart = createTimeChart(e.getValue(), dataManager.getReportsForApp(app)));
        timeLayout = new VerticalLayout(numberField);
        Style.NO_PADDING.apply(timeLayout);
        addComponent(timeLayout);
        List<ReportInfo> reportInfos = dataManager.getReportsForApp(app);
        timeChart = createTimeChart(30, reportInfos);
        versionChart = createVersionChart(reportInfos);
        setSizeFull();
        addAttachListener(e -> dataManager.addListener(this, ReportInfo.class));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    @NotNull
    private JFreeChartWrapper createTimeChart(int age, @NotNull List<ReportInfo> reports) {
        TimeSeries series = new TimeSeries("Date");
        series.setMaximumItemAge(age);
        series.add(new Day(new Date()), 0);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -age);
        series.add(new Day(start.getTime()), 0);
        for (ReportInfo report : reports) {
            Date date = report.getDate();
            Day day = new Day(date);
            int count = Optional.ofNullable(series.getDataItem(day)).map(TimeSeriesDataItem::getValue).map(Number::intValue).orElse(0);
            count++;
            series.addOrUpdate(day, count);
        }
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
        JFreeChartWrapper wrapper = new JFreeChartWrapper(chart);
        timeLayout.replaceComponent(timeChart, wrapper);
        return wrapper;
    }

    @NotNull
    private JFreeChartWrapper createVersionChart(@NotNull List<ReportInfo> reports) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (ReportInfo report : reports) {
            String version = report.getAndroidVersion();
            int count;
            if (dataset.getKeys().contains(version)) {
                count = dataset.getValue(version).intValue() + 1;
            } else {
                count = 1;
            }
            dataset.insertValue(0, version, count);
        }
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
        JFreeChartWrapper wrapper = new JFreeChartWrapper(chart);
        replaceComponent(versionChart, wrapper);
        return wrapper;
    }

    @Override
    public void onChange(@NotNull ReportInfo reportInfo) {
        if (reportInfo.getApp().equals(app)) {
            getUI().access(() -> {
                List<ReportInfo> reportInfos = dataManager.getReportsForApp(app);
                timeChart = createTimeChart(numberField.getValue(), reportInfos);
                versionChart = createVersionChart(reportInfos);
            });
        }
    }
}
