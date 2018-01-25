package com.faendir.acra.ui.view.base;

import com.faendir.acra.dataprovider.ObservableDataProvider;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> {
    public static final String CAPTION = "Reports";

    public ReportList(App app, @NonNull NavigationManager navigationManager, @NonNull Consumer<Report> reportDeleter, @NonNull ObservableDataProvider<Report, ?> reportProvider) {
        super(CAPTION, reportProvider);
        setId(CAPTION);
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), "date", "Date"), SortDirection.DESCENDING);
        addColumn(Report::getVersionCode, "versionCode", "App Version");
        addColumn(Report::getAndroidVersion, "androidVersion", "Android Version");
        addColumn(Report::getPhoneModel, "phoneModel", "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "stacktrace", "Stacktrace").setExpandRatio(1).setMinimumWidthFromContent(false);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete", new ButtonRenderer<>(e -> new Popup().setTitle("Confirm")
                    .addComponent(new Label("Are you sure you want to delete this report?"))
                    .addYesNoButtons(p -> reportDeleter.accept(e.getItem()), true))).setSortable(false);
        }
        addOnClickNavigation(navigationManager, ReportView.class, e -> e.getItem().getId());
    }
}
