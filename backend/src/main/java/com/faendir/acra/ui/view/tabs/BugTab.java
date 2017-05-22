package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.mongod.data.ReportUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.util.StringUtils;
import com.faendir.acra.util.Style;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Lukas
 * @since 17.05.2017
 */
public class BugTab extends CustomComponent implements DataManager.ReportChangeListener {
    public static final String CAPTION = "Bugs";
    private final VerticalLayout root;
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
        setCaption(CAPTION);
        addAttachListener(e -> dataManager.addListener(this));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    private void handleBugSelection(SelectionEvent<Bug> e) {
        if(root.getComponentCount() == 2){
            root.removeComponent(root.getComponent(1));
        }
        e.getFirstSelectedItem().ifPresent(bug -> root.addComponent(new ReportList(app, navigationManager, dataManager)));
    }

    @Override
    public void onChange() {
        bugs.setItems(ReportUtils.getBugs(dataManager.getReports(app)));
    }
}
