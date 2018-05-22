package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.view.VBug;
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
 * @since 21.03.2018
 */
@SpringComponent("bugReportTab")
@ViewScope
public class ReportTab implements BugTab {
    @NonNull private final DataService dataService;

    @Autowired
    public ReportTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull VBug bug, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(bug.getApp(), navigationManager, dataService::delete, dataService.getReportProvider(bug));
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
