package com.faendir.acra.ui.view.base;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.Report;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.renderers.ButtonRenderer;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> implements DataManager.ReportChangeListener {
    private final String app;
    private final DataManager dataManager;

    public ReportList(String app, NavigationManager navigationManager, DataManager dataManager) {
        super("Reports", dataManager.getReports(app));
        this.app = app;
        this.dataManager = dataManager;
        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), "Date"), SortDirection.DESCENDING);
        addColumn(Report::getVersionCode, "App Version");
        addColumn(Report::getAndroidVersion, "Android Version");
        addColumn(Report::getPhoneModel, "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete", new ButtonRenderer<>(e -> dataManager.remove(e.getItem())));
        }
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
        addAttachListener(e -> dataManager.addListener(this));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    @Override
    public void onChange() {
        setItems(dataManager.getReports(app));
    }
}
