package com.faendir.acra.ui.view.base;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.ReportInfo;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<ReportInfo> implements DataManager.Listener<ReportInfo> {
    public static final String CAPTION = "Reports";
    @NotNull private final Supplier<List<ReportInfo>> reportSupplier;
    @NotNull private final Function<ReportInfo, Boolean> relevanceFunction;

    public ReportList(String app, @NotNull NavigationManager navigationManager, @NotNull DataManager dataManager, @NotNull Supplier<List<ReportInfo>> reportSupplier,
                      @NotNull Function<ReportInfo, Boolean> relevanceFunction) {
        super(CAPTION, reportSupplier.get());
        setId(CAPTION);
        this.reportSupplier = reportSupplier;
        this.relevanceFunction = relevanceFunction;
        setWidth(100, Unit.PERCENTAGE);
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(ReportInfo::getDate, new TimeSpanRenderer(), "Date"), SortDirection.DESCENDING);
        addColumn(ReportInfo::getVersionCode, "App Version");
        addColumn(ReportInfo::getAndroidVersion, "Android Version");
        addColumn(ReportInfo::getPhoneModel, "Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete", new ButtonRenderer<>(e -> dataManager.deleteReport(e.getItem())));
        }
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
        addAttachListener(e -> dataManager.addListener(this, ReportInfo.class));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    @Override
    public void onChange(@NotNull ReportInfo info) {
        if (relevanceFunction.apply(info)) {
            setItems();
        }
    }

    private void setItems() {
        getUI().access(() -> setItems(reportSupplier.get()));
    }
}
