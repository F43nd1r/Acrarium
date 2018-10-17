package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.view.app.AppView;
import com.faendir.acra.ui.view.bug.BugView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "bug", layout = AppView.class)
public class BugTab extends AppTab<VerticalLayout> {
    @Autowired
    public BugTab(DataService dataService) {
        super(dataService);
    }

    @Override
    void init(App app) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        getContent().add(header);
        getContent().setAlignItems(FlexComponent.Alignment.START);
        Checkbox hideSolved = new Checkbox("Hide solved", true);
        MyGrid<VBug> bugs = new MyGrid<>(getDataService().getBugProvider(app, hideSolved::getValue));
        bugs.setSelectionMode(Grid.SelectionMode.MULTI);
        hideSolved.addValueChangeListener(e -> getUI().ifPresent(ui-> ui.access(() -> {
            bugs.deselectAll();
            bugs.getDataProvider().refreshAll();
        })));
        Button merge = new Button("Merge bugs", e -> {
            List<VBug> selectedItems = new ArrayList<>(bugs.getSelectedItems());
            if (selectedItems.size() > 1) {
                RadioButtonGroup<String> titles = new RadioButtonGroup<>();
                titles.setItems(selectedItems.stream().map(bug -> bug.getBug().getTitle()).collect(Collectors.toList()));
                titles.setValue(selectedItems.get(0).getBug().getTitle());
                new Popup().setTitle("Choose title for bug group").addComponent(titles).addCreateButton(p -> {
                    getDataService().mergeBugs(selectedItems.stream().map(VBug::getBug).collect(Collectors.toList()), titles.getValue());
                    bugs.deselectAll();
                    bugs.getDataProvider().refreshAll();
                }, true).show();
            } else {
                Notification.show("Please select at least two bugs");
            }
        });
        header.add(merge, hideSolved);
        header.setSpacing(false);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bugs.addColumn(VBug::getReportCount,QReport.report.count(), "Reports");
        bugs.addColumn(new TimeSpanRenderer<>(VBug::getLastReport), QReport.report.date.max(), "Latest Report");
        bugs.addColumn(VBug::getHighestVersionCode, QReport.report.stacktrace.version.code.max(), "Latest Version");
        bugs.addColumn(VBug::getUserCount, QReport.report.installationId.countDistinct(), "Affected Users");
        bugs.addColumn(bug -> bug.getBug().getTitle(), QBug.bug.title, "Title").setFlexGrow(1);
        bugs.addColumn(new ComponentRenderer<>(bug -> {
            Checkbox checkbox = new Checkbox(bug.getBug().isSolved());
            checkbox.setEnabled(SecurityUtils.hasPermission(app, Permission.Level.EDIT));
            checkbox.addValueChangeListener(e -> getDataService().setBugSolved(bug.getBug(), e.getValue()));
            return checkbox;
        }), QBug.bug.solved, "Solved");
        bugs.addOnClickNavigation(BugView.class, bug -> bug.getBug().getId());
        getContent().removeAll();
        getContent().add(bugs);
        getContent().setFlexGrow(1, bugs);
        getContent().setSizeFull();
    }
}
