package com.faendir.acra.ui.view.base;

import com.faendir.acra.model.QReport;
import com.faendir.acra.service.data.DataService;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.data.HasValue;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;
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
import org.springframework.lang.NonNull;
import org.vaadin.addon.JFreeChartWrapper;
import org.vaadin.risto.stepper.IntStepper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author lukas
 * @since 21.05.18
 */
public class Statistics extends Composite {
    public static final Color BACKGROUND_GRAY = new Color(0xfafafa); //vaadin gray
    public static final Color BLUE = new Color(0x197de1); //vaadin blue
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;
    private final BooleanExpression baseExpression;
    private final DataService dataService;
    private final List<Filter<?, ?>> filters;
    private final TimeChart timeChart;
    private final PieChart androidVersionChart;
    private final PieChart appVersionChart;

    public Statistics(BooleanExpression baseExpression, DataService dataService) {
        this.baseExpression = baseExpression;
        this.dataService = dataService;
        filters = new ArrayList<>();
        GridLayout filterLayout = new GridLayout(2, 1);
        filterLayout.setSpacing(true);
        filterLayout.setWidth(WIDTH, Unit.PIXELS);
        filterLayout.setHeight(HEIGHT, Unit.PIXELS);
        filterLayout.setColumnExpandRatio(1, 1);

        Label title = new Label("Filter");
        title.addStyleName(ValoTheme.LABEL_H2);
        filterLayout.space();
        filterLayout.addComponent(title);

        IntStepper dayStepper = new IntStepper();
        dayStepper.setValue(30);
        dayStepper.setMinValue(1);
        filters.add(new Filter<>("Last X days", dayStepper, days -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            return QReport.report.date.after(calendar.getTime());
        }));

        ComboBox<String> androidVersionBox = new ComboBox<>(null, dataService.getFromReports(baseExpression, QReport.report.androidVersion));
        androidVersionBox.setEmptySelectionAllowed(false);
        filters.add(new Filter<>("Android Version", androidVersionBox, QReport.report.androidVersion::eq));

        ComboBox<Integer> appVersionBox = new ComboBox<>(null, dataService.getFromReports(baseExpression, QReport.report.versionCode));
        appVersionBox.setEmptySelectionAllowed(false);
        filters.add(new Filter<>("App Version", appVersionBox, QReport.report.versionCode::eq));

        filterLayout.addComponents(filters.stream().flatMap(filter -> filter.getComponents().stream()).toArray(Component[]::new));

        Button applyButton = new Button("Apply", e -> update());
        applyButton.setWidth(100, Unit.PERCENTAGE);
        filterLayout.space();
        filterLayout.addComponent(applyButton);

        timeChart = new TimeChart("Reports over time");
        androidVersionChart = new PieChart("Reports per Android Version");
        appVersionChart = new PieChart("Reports per App Version");
        CssLayout root = new CssLayout(filterLayout, timeChart, androidVersionChart, appVersionChart);
        setCompositionRoot(root);
        update();
    }

    private void update() {
        BooleanExpression expression = baseExpression;
        for (Filter filter : filters) {
            expression = filter.apply(expression);
        }
        timeChart.setContent(dataService.countReports(expression, QReport.report.date.year().multiply(512).add(QReport.report.date.dayOfYear()), QReport.report.date));
        androidVersionChart.setContent(dataService.countReports(expression, QReport.report.androidVersion, QReport.report.androidVersion));
        appVersionChart.setContent(dataService.countReports(expression, QReport.report.versionName, QReport.report.versionName));
    }

    private static class Filter<T, C extends Component & HasValue<T>> {
        private final CheckBox checkBox;
        private final C c;
        private final Function<T, BooleanExpression> filter;

        Filter(String title, C c, Function<T, BooleanExpression> filter) {
            checkBox = new CheckBox(title);
            this.c = c;
            this.filter = filter;
            c.setEnabled(false);
            c.setWidth(100, Unit.PERCENTAGE);
            checkBox.setValue(false);
            checkBox.addValueChangeListener(e -> c.setEnabled(e.getValue()));
        }

        public List<Component> getComponents() {
            return Arrays.asList(checkBox, c);
        }

        public BooleanExpression apply(BooleanExpression booleanExpression) {
            if (checkBox.getValue() && c.getValue() != null) {
                return booleanExpression.and(filter.apply(c.getValue()));
            }
            return booleanExpression;
        }
    }

    private static abstract class BaseChart<T> extends Composite {
        private final Panel panel;

        BaseChart(@NonNull String caption) {
            panel = new Panel();
            panel.setCaption(caption);
            panel.setSizeUndefined();
            setCompositionRoot(panel);
        }

        public void setContent(@NonNull Map<T, Long> map){
            JFreeChartWrapper content = new JFreeChartWrapper(createChart(map));
            content.setWidth(WIDTH, Unit.PIXELS);
            content.setHeight(HEIGHT, Unit.PIXELS);
            panel.setContent(content);
        }

        protected abstract JFreeChart createChart(@NonNull Map<T, Long> map);
    }

    private static class TimeChart extends BaseChart<Date> {

        TimeChart(@NonNull String caption) {
            super(caption);
        }

        @Override
        public JFreeChart createChart(@NonNull Map<Date, Long> map) {
            TimeSeries series = new TimeSeries("Date");
            series.add(new Day(new Date()), 0);
            map.forEach((date, count) -> series.addOrUpdate(new Day(date), count));
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
            return chart;
        }
    }

    private static class PieChart extends BaseChart<String> {

        PieChart(@NonNull String caption) {
            super(caption);
        }

        @Override
        public JFreeChart createChart(@NonNull Map<String, Long> map) {
            DefaultPieDataset dataset = new DefaultPieDataset();
            map.forEach((label, count) -> dataset.insertValue(0, label, count));
            dataset.sortByKeys(SortOrder.ASCENDING);
            JFreeChart chart = ChartFactory.createPieChart("", dataset, false, false, false);
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
            return chart;
        }
    }
}
