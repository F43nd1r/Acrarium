package com.faendir.acra.ui.view.base;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> {
    public static final String CAPTION = "Reports";

    public ReportList(App app, @NonNull NavigationManager navigationManager, @NonNull DataManager dataManager, @NonNull DataProvider<Report, Void> reportProvider) {
        super(CAPTION, reportProvider);
        setId(CAPTION);
        setWidth(100, Unit.PERCENTAGE);
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), "date", "Date"), SortDirection.DESCENDING);
        addColumn(Report::getVersionCode, "versionCode", "App Version");
        addColumn(Report::getAndroidVersion, "androidVersion", "Android Version");
        addColumn(Report::getPhoneModel, "phoneModel", "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "stacktrace", "Stacktrace").setExpandRatio(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete", new ButtonRenderer<>(e -> dataManager.deleteReport(e.getItem()))).setSortable(false);
        }
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
    }
}
