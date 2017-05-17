package com.faendir.acra.ui.view;

import com.faendir.acra.data.Bug;
import com.faendir.acra.data.Report;
import com.faendir.acra.data.ReportUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.util.StringUtils;
import com.faendir.acra.util.Style;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

/**
 * @author Lukas
 * @since 17.05.2017
 */
public class BugTab extends CustomComponent {
    private final VerticalLayout root;
    private final NavigationManager navigationManager;

    public BugTab(List<Report> reportList, NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
        MyGrid<Bug> bugs = new MyGrid<>(null, ReportUtils.getBugs(reportList));
        bugs.setSizeFull();
        bugs.addColumn(bug -> String.valueOf(bug.getReports().size()), "Reports");
        bugs.addColumn(bug -> StringUtils.distanceFromNowAsString(bug.getLastDate()), "Latest Report");
        bugs.addColumn(bug -> String.valueOf(bug.getVersionCode()), "Version");
        bugs.addColumn(bug -> bug.getTrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        bugs.addSelectionListener(this::handleBugSelection);
        root = new VerticalLayout(bugs);
        Style.NO_PADDING.apply(root);
        root.setSizeFull();
        setCompositionRoot(root);
        setSizeFull();
        setCaption("Bugs");
    }

    private void handleBugSelection(SelectionEvent<Bug> e) {
        if(root.getComponentCount() == 2){
            root.removeComponent(root.getComponent(1));
        }
        e.getFirstSelectedItem().ifPresent(bug -> root.addComponent(new ReportList(bug.getReports(), navigationManager)));
    }
}
