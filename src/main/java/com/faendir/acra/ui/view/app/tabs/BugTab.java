/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.Popup;
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
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 17.05.2017
 */
@SpringComponent
@ViewScope
public class BugTab implements AppTab {
    @NonNull private final DataService dataService;

    @Autowired
    public BugTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public String getCaption() {
        return "Bugs";
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth(100, Sizeable.Unit.PERCENTAGE);
        header.addStyleName(AcraTheme.PADDING_TOP);
        layout.addComponent(header);
        layout.setComponentAlignment(header, Alignment.MIDDLE_LEFT);
        CheckBox hideSolved = new CheckBox("Hide solved", true);
        MyGrid<VBug> bugs = new MyGrid<>(null, dataService.getBugProvider(app, hideSolved::getValue));
        bugs.setSelectionMode(Grid.SelectionMode.MULTI);
        hideSolved.addValueChangeListener(e -> layout.getUI().access(() -> {
            bugs.deselectAll();
            bugs.getDataProvider().refreshAll();
        }));
        Button merge = new Button("Merge bugs", e -> {
            List<VBug> selectedItems = new ArrayList<>(bugs.getSelectedItems());
            if (selectedItems.size() > 1) {
                RadioButtonGroup<String> titles = new RadioButtonGroup<>("", selectedItems.stream().map(bug -> bug.getBug().getTitle()).collect(Collectors.toList()));
                titles.setSelectedItem(selectedItems.get(0).getBug().getTitle());
                new Popup().setTitle("Choose title for bug group").addComponent(titles).addCreateButton(p -> {
                    dataService.mergeBugs(selectedItems.stream().map(VBug::getBug).collect(Collectors.toList()), titles.getSelectedItem().orElseThrow(IllegalStateException::new));
                    bugs.deselectAll();
                    bugs.getDataProvider().refreshAll();
                }, true).show();
            } else {
                Notification.show("Please select at least two bugs", Notification.Type.ERROR_MESSAGE);
            }
        });
        header.addComponent(merge);
        header.addComponent(hideSolved);
        header.setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        bugs.addColumn(VBug::getReportCount,QReport.report.count(), "Reports");
        bugs.sort(bugs.addColumn(VBug::getLastReport, new TimeSpanRenderer(), QReport.report.date.max(), "Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(bug -> bug.getBug().getTitle(), QBug.bug.title, "Title").setExpandRatio(1).setMinimumWidthFromContent(false);
        bugs.addOnClickNavigation(navigationManager, com.faendir.acra.ui.view.bug.BugView.class, bugItemClick -> String.valueOf(bugItemClick.getItem().getBug().getId()));
        bugs.addColumn(bug -> new MyCheckBox(bug.getBug().isSolved(), SecurityUtils.hasPermission(app, Permission.Level.EDIT), e -> dataService.setBugSolved(bug.getBug(), e.getValue())),
                new ComponentRenderer(), QBug.bug.solved, "Solved");
        layout.addComponent(bugs);
        layout.setExpandRatio(bugs, 1);
        layout.setSizeFull();
        layout.addStyleName(AcraTheme.NO_PADDING);
        return layout;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
