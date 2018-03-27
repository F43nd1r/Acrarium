package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.sql.util.CountResult;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.ui.view.bug.BugView;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 17.05.2017
 */
@SpringComponent
@ViewScope
public class BugTab implements AppTab {
    public static final String CAPTION = "Bugs";
    @NonNull private final BugRepository bugRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final BufferedDataProvider.Factory factory;

    @Autowired
    public BugTab(@NonNull BugRepository bugRepository, @NonNull ReportRepository reportRepository, @NonNull BufferedDataProvider.Factory factory) {
        this.bugRepository = bugRepository;
        this.reportRepository = reportRepository;
        this.factory = factory;
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Style.PADDING_TOP.apply(header);
        layout.addComponent(header);
        layout.setComponentAlignment(header, Alignment.MIDDLE_LEFT);
        CheckBox hideSolved = new CheckBox("Hide solved", true);
        MyGrid<Bug> bugs = new MyGrid<>(null, createDataProvider(app, true));
        bugs.setSelectionMode(Grid.SelectionMode.MULTI);
        hideSolved.addValueChangeListener(e -> layout.getUI().access(() -> {
            Set<Bug> selection = bugs.getSelectedItems();
            bugs.setDataProvider(createDataProvider(app, e.getValue()));
            selection.forEach(bugs::select);
        }));
        Button merge = new Button("Merge bugs", e -> {
            List<Bug> selectedItems = new ArrayList<>(bugRepository.loadStacktraces(bugs.getSelectedItems()));
            if (selectedItems.size() > 1) {
                RadioButtonGroup<String> titles = new RadioButtonGroup<>("", selectedItems.stream().map(Bug::getTitle).collect(Collectors.toList()));
                titles.setSelectedItem(selectedItems.get(0).getTitle());
                new Popup().setTitle("Choose title for bug group").addComponent(titles).addCreateButton(p -> {
                    Bug bug = selectedItems.remove(0);
                    bug.setTitle(titles.getSelectedItem().orElseThrow(IllegalStateException::new));
                    bug.getStacktraces().addAll(selectedItems.stream().flatMap(b -> b.getStacktraces().stream()).collect(Collectors.toList()));
                    bugRepository.save(bug);
                    for (Bug b : selectedItems) {
                        Slice<Report> reports = reportRepository.findAllByBug(b, PageRequest.of(0, Integer.MAX_VALUE));
                        reports.forEach(report -> report.setBug(bug));
                        reportRepository.saveAll(reports);
                    }
                    bugRepository.deleteAll(selectedItems);
                    bugs.setDataProvider(createDataProvider(app, hideSolved.getValue()));
                    p.close();
                }).show();
            } else {
                Notification.show("Please select at least two bugs", Notification.Type.ERROR_MESSAGE);
            }
        });
        header.addComponent(merge);
        header.addComponent(hideSolved);
        header.setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        Map<Integer, Long> counts = reportRepository.countAllByBug().stream().collect(Collectors.toMap(CountResult::getGroup, CountResult::getCount));
        bugs.addColumn(bug -> counts.get(bug.getId()), "Reports");
        bugs.sort(bugs.addColumn(Bug::getLastReport, new TimeSpanRenderer(), "lastReport", "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "versionCode", "Version");
        bugs.addColumn(Bug::getTitle, "stacktrace", "Stacktrace").setExpandRatio(1).setMinimumWidthFromContent(false);
        bugs.addOnClickNavigation(navigationManager, BugView.class, bugItemClick -> String.valueOf(bugItemClick.getItem().getId()));
        bugs.addColumn(bug -> new MyCheckBox(bug.isSolved(), SecurityUtils.hasPermission(app, Permission.Level.EDIT), e -> {
            bug.setSolved(e.getValue());
            bugRepository.save(bug);
        }), new ComponentRenderer(), "Solved");
        layout.addComponent(bugs);
        layout.setExpandRatio(bugs, 1);
        layout.setSizeFull();
        Style.NO_PADDING.apply(layout);
        return layout;
    }

    private BufferedDataProvider<Bug> createDataProvider(@NonNull App app, boolean hideSolved) {
        return factory.create(app, hideSolved ? bugRepository::findAllByAppAndSolvedFalse : bugRepository::findAllByApp,
                hideSolved ? bugRepository::countAllByAppAndSolvedFalse : bugRepository::countAllByApp);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
