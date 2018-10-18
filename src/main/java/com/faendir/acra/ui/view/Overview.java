package com.faendir.acra.ui.view;

import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@Route(value = "", layout = MainView.class)
public class Overview extends VerticalLayout implements ComponentEventListener<AttachEvent>, HasRoute {
    private final DataService dataService;

    @Autowired
    public Overview(DataService dataService) {
        this.dataService = dataService;
        addAttachListener(this);
    }

    @Override
    public void onComponentEvent(AttachEvent event) {
        removeAll();
        MyGrid<VApp> grid = new MyGrid<>(dataService.getAppProvider());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(VApp::getName, QApp.app.name, "Name");
        grid.addColumn(VApp::getBugCount, QBug.bug.countDistinct(), "Bugs");
        grid.addColumn(VApp::getReportCount, QReport.report.count(), "Reports");
        grid.addOnClickNavigation(BugTab.class, VApp::getId);
        setSizeFull();
        add(grid);
    }

    @Override
    public Path.Element<?> getPathElement() {
        return new Path.Element<>(getClass(), "Acrarium");
    }

    @Override
    public Class<? extends HasRoute> getLogicalParent() {
        return null;
    }
}
