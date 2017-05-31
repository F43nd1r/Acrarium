package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.data.ReportUtils;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

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
    private final CheckBox hideSolved;

    public BugTab(String app, NavigationManager navigationManager, DataManager dataManager) {
        this.app = app;
        this.navigationManager = navigationManager;
        this.dataManager = dataManager;
        hideSolved = new CheckBox("Hide solved", true);
        hideSolved.addValueChangeListener(e -> onChange());
        addComponent(hideSolved);
        setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        bugs = new MyGrid<>(null, getBugs());
        bugs.setSizeFull();
        bugs.addColumn(dataManager::countReportsForBug, "Reports");
        bugs.sort(bugs.addColumn(bug -> ReportUtils.getLastReportDate(dataManager.getReportsForBug(bug)), new TimeSpanRenderer(), "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "Version");
        bugs.addColumn(bug -> bug.getStacktrace().split("\n", 2)[0], "Stacktrace").setExpandRatio(1);
        bugs.addSelectionListener(this::handleBugSelection);
        bugs.addComponentColumn(bug -> new MyCheckBox(bug.isSolved(), SecurityUtils.hasPermission(app, Permission.Level.EDIT), e -> {
            dataManager.setBugSolved(bug, e.getValue());
            onChange();
        })).setCaption("Solved");
        addComponent(bugs);
        setExpandRatio(bugs, 1);
        Style.NO_PADDING.apply(this);
        setSizeFull();
        setCaption(CAPTION);
        addAttachListener(e -> dataManager.addListener(this));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    private void handleBugSelection(SelectionEvent<Bug> e) {
        for (Component component : this) {
            if (ReportList.CAPTION.equals(component.getId())) {
                removeComponent(component);
            }
        }
        e.getFirstSelectedItem().ifPresent(bug -> {
            ReportList reportList = new ReportList(app, navigationManager, dataManager, () -> dataManager.getReportsForBug(bug));
            addComponent(reportList);
            setExpandRatio(reportList, 1);
        });
    }

    @Override
    public void onChange() {
        bugs.setItems(getBugs());
    }

    private List<Bug> getBugs() {
        List<Bug> bugs = dataManager.getBugs(app);
        if (hideSolved.getValue()) {
            return bugs.stream().filter(bug -> !bug.isSolved()).collect(Collectors.toList());
        }
        return bugs;
    }
}
