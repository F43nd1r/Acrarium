package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.util.CountResult;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 17.05.2017
 */
@SpringComponent
@ViewScope
public class BugTab implements MyTabSheet.Tab {
    public static final String CAPTION = "Bugs";
    @NonNull private final BugRepository bugRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final BufferedDataProvider.Factory factory;
    @Nullable private ReportList reportList;

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
        CheckBox hideSolved = new CheckBox("Hide solved", true);
        layout.addComponent(hideSolved);
        layout.setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        MyGrid<Bug> bugs = new MyGrid<>(null, createDataProvider(app, true));
        hideSolved.addValueChangeListener(e -> layout.getUI().access(() -> {
            Set<Bug> selection = bugs.getSelectedItems();
            bugs.setDataProvider(createDataProvider(app, e.getValue()));
            selection.forEach(bugs::select);
        }));
        Map<Integer, Long> counts = reportRepository.countAllByBug().stream().collect(Collectors.toMap(CountResult::getGroup, CountResult::getCount));
        bugs.addColumn(bug -> counts.get(bug.getId()), "Reports");
        bugs.sort(bugs.addColumn(Bug::getLastReport, new TimeSpanRenderer(), "lastReport", "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "versionCode", "Version");
        bugs.addColumn(bug -> bug.getStacktrace().split("\n", 2)[0], "stacktrace", "Stacktrace").setExpandRatio(1).setMinimumWidthFromContent(false);
        bugs.addSelectionListener(event -> {
            Optional<Bug> selection = event.getFirstSelectedItem();
            ReportList reports = null;
            if (selection.isPresent()) {
                reports = new ReportList(app, navigationManager, reportRepository::delete,
                        factory.create(selection.get(), reportRepository::findAllByBug, reportRepository::countAllByBug));
                reports.setSizeFull();
                layout.replaceComponent(this.reportList, reports);
                layout.setExpandRatio(reports, 1);
            } else if (this.reportList != null) {
                layout.removeComponent(this.reportList);
            }
            this.reportList = reports;
        });
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
}
