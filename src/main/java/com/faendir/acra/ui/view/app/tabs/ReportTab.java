package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.data.DataService;
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
    @NonNull private final DataService dataService;

    @Autowired
    public ReportTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(app, navigationManager, dataService::delete, dataService.getReportProvider(app));
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
