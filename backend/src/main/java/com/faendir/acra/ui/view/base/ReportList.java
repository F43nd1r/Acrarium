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

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> implements DataManager.ReportChangeListener {
    public static final String CAPTION = "Reports";
    private final Supplier<List<Report>> reportSupplier;

    public ReportList(String app, NavigationManager navigationManager, DataManager dataManager, Supplier<List<Report>> reportSupplier) {
        super(CAPTION, reportSupplier.get());
        setId(CAPTION);
        this.reportSupplier = reportSupplier;
        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), "Date"), SortDirection.DESCENDING);
        addColumn(Report::getVersionCode, "App Version");
        addColumn(Report::getAndroidVersion, "Android Version");
        addColumn(Report::getPhoneModel, "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete", new ButtonRenderer<>(e -> dataManager.deleteReport(e.getItem())));
        }
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
        addAttachListener(e -> dataManager.addListener(this));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    @Override
    public void onChange() {
        setItems(reportSupplier.get());
    }
}
