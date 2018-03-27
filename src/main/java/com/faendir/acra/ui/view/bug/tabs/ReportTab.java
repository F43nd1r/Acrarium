package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent("bugReportTab")
@ViewScope
public class ReportTab implements BugTab {
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final BufferedDataProvider.Factory factory;

    @Autowired
    public ReportTab(@NonNull ReportRepository reportRepository, @NonNull BufferedDataProvider.Factory factory) {
        this.reportRepository = reportRepository;
        this.factory = factory;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(bug.getApp(), navigationManager, reportRepository::delete,
                factory.create(bug, reportRepository::findAllByBug, reportRepository::countAllByBug));
        content.setSizeFull();
        return content;
    }

    @Override
    public String getCaption() {
        return ReportList.CAPTION;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
