package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.model.App;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 29.05.2017
 */
@SpringComponent
@ViewScope
public class ReportTab implements AppTab {
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final BufferedDataProvider.Factory factory;

    @Autowired
    public ReportTab(@NonNull ReportRepository reportRepository, @NonNull BufferedDataProvider.Factory factory) {
        this.reportRepository = reportRepository;
        this.factory = factory;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(app, navigationManager, reportRepository::delete,
                factory.create(app, reportRepository::findAllByBugApp, reportRepository::countByBugApp));
        content.setSizeFull();
        return content;
    }

    @Override
    public String getCaption() {
        return ReportList.CAPTION;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
