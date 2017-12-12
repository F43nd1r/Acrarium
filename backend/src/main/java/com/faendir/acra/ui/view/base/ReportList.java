package com.faendir.acra.ui.view.base;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.ReportInfo;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<ReportInfo> implements DataManager.Listener<ReportInfo> {
    public static final String CAPTION = "Reports";
    @NotNull private final DataProvider<ReportInfo, Void> reportSupplier;
    @NotNull private final Function<ReportInfo, Boolean> relevanceFunction;

    public ReportList(String app, @NotNull NavigationManager navigationManager, @NotNull DataManager dataManager, @NotNull DataProvider<ReportInfo, Void> reportSupplier,
                      @NotNull Function<ReportInfo, Boolean> relevanceFunction) {
        super(CAPTION, reportSupplier);
        setId(CAPTION);
        this.reportSupplier = reportSupplier;
        this.relevanceFunction = relevanceFunction;
        setWidth(100, Unit.PERCENTAGE);
        setSelectionMode(SelectionMode.NONE);
        sort(addColumn(ReportInfo::getDate, new TimeSpanRenderer(), "content.map.USER_CRASH_DATE", "Date"), SortDirection.DESCENDING);
        addColumn(ReportInfo::getVersionCode, "content.map.APP_VERSION_CODE","App Version");
        addColumn(ReportInfo::getAndroidVersion, "content.map.ANDROID_VERSION","Android Version");
        addColumn(ReportInfo::getPhoneModel, "content.map.PHONE_MODEL","Device");
        addColumn(report -> report.getStacktrace().split("\n", 2)[0], "content.map.STACK_TRACE","Stacktrace").setExpandRatio(1);
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
        getUI().access(reportSupplier::refreshAll);
    }
}
