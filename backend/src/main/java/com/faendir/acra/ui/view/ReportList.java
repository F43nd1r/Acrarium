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
        addColumn(report -> String.valueOf(report.getVersionCode()), "App Version");
        addColumn(Report::getAndroidVersion, "Android Version");
        addColumn(Report::getPhoneModel, "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        addColumn(report -> "Delete", new ButtonRenderer<>(e -> reportManager.remove(e.getItem())));
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
        addAttachListener(e -> reportManager.addListener(this));
        addDetachListener(e -> reportManager.removeListener(this));
    }

    @Override
    public void onChange() {
        setItems(reportManager.getReports(app));
    }
}
