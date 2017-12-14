package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * @author Lukas
 * @since 17.05.2017
 */
public class BugTab extends VerticalLayout implements MyTabSheet.Tab {
    public static final String CAPTION = "Bugs";
    @Nullable private ReportList reportList;

    public BugTab() {
        setCaption(CAPTION);
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager) {
        CheckBox hideSolved = new CheckBox("Hide solved", true);
        addComponent(hideSolved);
        setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        MyGrid<Bug> bugs = new MyGrid<>(null, dataManager.lazyGetBugs(app, true));
        hideSolved.addValueChangeListener(e -> getUI().access(() -> {
            Set<Bug> selection = bugs.getSelectedItems();
            bugs.setDataProvider(dataManager.lazyGetBugs(app, e.getValue()));
            selection.forEach(bugs::select);
        }));
        //bugs.setWidth(100, Unit.PERCENTAGE);
        bugs.setSizeFull();
        bugs.addColumn(dataManager::reportCountForBug, "Reports");
        bugs.sort(bugs.addColumn(Bug::getLastReport, new TimeSpanRenderer(), "lastReport", "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "versionCode", "Version");
        bugs.addColumn(bug -> bug.getStacktrace().split("\n", 2)[0], "stacktrace", "Stacktrace").setExpandRatio(1);
        bugs.addSelectionListener(event -> {
            Optional<Bug> selection = event.getFirstSelectedItem();
            ReportList reports = null;
            if (selection.isPresent()) {
                reports = new ReportList(app, navigationManager, dataManager, dataManager.lazyGetReportsForBug(selection.get()));
                reports.setSizeFull();
                replaceComponent(this.reportList, reports);
                setExpandRatio(reports, 1);
            } else if (this.reportList != null) {
                removeComponent(this.reportList);
            }
            this.reportList = reports;
        });
        bugs.addColumn(bug -> new MyCheckBox(bug.isSolved(), SecurityUtils.hasPermission(app, Permission.Level.EDIT), e -> dataManager.setBugSolved(bug, e.getValue())),
                       new ComponentRenderer(), "Solved");
        addComponent(bugs);
        setExpandRatio(bugs, 1);
        setSizeFull();
        Style.NO_PADDING.apply(this);
        return this;
    }
}
