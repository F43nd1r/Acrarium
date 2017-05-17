package com.faendir.acra.ui.view;

import com.faendir.acra.data.Report;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.util.StringUtils;

import java.util.List;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> {
    public ReportList(List<Report> reportList, NavigationManager navigationManager) {
        super("Reports", reportList);
        setSizeFull();
        addColumn(report -> StringUtils.distanceFromNowAsString(report.getDate()), "Date");
        addReportColumn("APP_VERSION_NAME", "App Version");
        addReportColumn( "ANDROID_VERSION", "Android Version");
        addReportColumn("PHONE_MODEL", "Device");
        addColumn(report -> report.getContent().getString("STACK_TRACE").split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        addItemClickListener(e -> navigationManager.navigateTo(ReportView.class, e.getItem().getId()));
    }


    private void addReportColumn(String key, String caption) {
        addColumn(report -> report.getContent().getString(key), caption);
    }
}
