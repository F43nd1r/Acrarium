package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.data.ReportUtils;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Lukas
 * @since 17.05.2017
 */
public class BugTab extends VerticalLayout implements DataManager.ReportChangeListener {
    public static final String CAPTION = "Bugs";
    private final String app;
    private final NavigationManager navigationManager;
    private final DataManager dataManager;
    private final MyGrid<Bug> bugs;

    public BugTab(String app, NavigationManager navigationManager, DataManager dataManager) {
        this.app = app;
        this.navigationManager = navigationManager;
        this.dataManager = dataManager;
        bugs = new MyGrid<>(null, ReportUtils.getBugs(dataManager.getReports(app)));
        bugs.setSizeFull();
        bugs.addColumn(bug -> bug.getReports().size(), "Reports");
        bugs.sort(bugs.addColumn(Bug::getLastDate, new TimeSpanRenderer(), "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "Version");
        bugs.addColumn(bug -> bug.getTrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        bugs.addSelectionListener(this::handleBugSelection);
        addComponent(bugs);
        Style.NO_PADDING.apply(this);
        setSizeFull();
        setCaption(CAPTION);
        addAttachListener(e -> dataManager.addListener(this));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    private void handleBugSelection(SelectionEvent<Bug> e) {
        if (getComponentCount() == 2) {
            removeComponent(getComponent(1));
        }
        e.getFirstSelectedItem().ifPresent(bug -> addComponent(new ReportList(app, navigationManager, dataManager, bug::getReports)));
    }

    @Override
    public void onChange() {
        bugs.setItems(ReportUtils.getBugs(dataManager.getReports(app)));
    }
}
