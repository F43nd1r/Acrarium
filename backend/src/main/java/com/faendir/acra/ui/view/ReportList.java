package com.faendir.acra.ui.view;

import com.faendir.acra.data.Report;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.util.StringUtils;
import com.vaadin.ui.renderers.ButtonRenderer;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> implements ReportManager.ChangeListener {
    private final String app;
    private final ReportManager reportManager;

    public ReportList(String app, NavigationManager navigationManager, ReportManager reportManager) {
        super("Reports", reportManager.getReports(app));
        this.app = app;
        this.reportManager = reportManager;
        setSizeFull();
        addColumn(report -> StringUtils.distanceFromNowAsString(report.getDate()), "Date");
        addReportColumn("APP_VERSION_NAME", "App Version");
        addReportColumn("ANDROID_VERSION", "Android Version");
        addReportColumn("PHONE_MODEL", "Device");
        addColumn(report -> report.getContent().getString("STACK_TRACE").split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        addColumn(report -> "Delete", new ButtonRenderer<>(e -> reportManager.remove(e.getItem())));
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
        addAttachListener(e -> reportManager.addListener(this));
        addDetachListener(e -> reportManager.removeListener(this));
    }


    private void addReportColumn(String key, String caption) {
        addColumn(report -> report.getContent().getString(key), caption);
    }

    @Override
    public void onChange() {
        setItems(reportManager.getReports(app));
    }
}
