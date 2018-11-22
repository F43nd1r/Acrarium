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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.app.AppView;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
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
        Translatable<Checkbox> hideSolved = Translatable.createCheckbox(true, Messages.HIDE_SOLVED);
        MyGrid<VBug> bugs = new MyGrid<>(getDataService().getBugProvider(app, () -> hideSolved.getContent().getValue()));
        bugs.setSelectionMode(Grid.SelectionMode.MULTI);
        hideSolved.getContent().addValueChangeListener(e -> getUI().ifPresent(ui -> ui.access(() -> {
            bugs.deselectAll();
            bugs.getDataProvider().refreshAll();
        })));
        Translatable<Button> merge = Translatable.createButton(e -> {
            List<VBug> selectedItems = new ArrayList<>(bugs.getSelectedItems());
            if (selectedItems.size() > 1) {
                RadioButtonGroup<String> titles = new RadioButtonGroup<>();
                titles.setItems(selectedItems.stream().map(bug -> bug.getBug().getTitle()).collect(Collectors.toList()));
                titles.setValue(selectedItems.get(0).getBug().getTitle());
                new Popup().setTitle(Messages.CHOOSE_BUG_GROUP_TITLE).addComponent(titles).addCreateButton(p -> {
                    getDataService().mergeBugs(selectedItems.stream().map(VBug::getBug).collect(Collectors.toList()), titles.getValue());
                    bugs.deselectAll();
                    bugs.getDataProvider().refreshAll();
                }, true).show();
            } else {
                Notification.show(Messages.ONLY_ONE_BUG_SELECTED);
            }
        }, Messages.MERGE_BUGS);
        header.add(merge, hideSolved);
        header.setSpacing(false);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bugs.addColumn(VBug::getReportCount, QReport.report.count(), Messages.REPORTS);
        bugs.addColumn(new TimeSpanRenderer<>(VBug::getLastReport), QReport.report.date.max(), Messages.LATEST_REPORT);
        bugs.addColumn(VBug::getHighestVersionCode, QReport.report.stacktrace.version.code.max(), Messages.LATEST_VERSION);
        bugs.addColumn(VBug::getUserCount, QReport.report.installationId.countDistinct(), Messages.AFFECTED_USERS);
        bugs.addColumn(bug -> bug.getBug().getTitle(), QBug.bug.title, Messages.TITLE).setFlexGrow(1);
        bugs.addColumn(new ComponentRenderer<>(bug -> {
            Checkbox checkbox = new Checkbox(bug.getBug().isSolved());
            checkbox.setEnabled(SecurityUtils.hasPermission(app, Permission.Level.EDIT));
            checkbox.addValueChangeListener(e -> getDataService().setBugSolved(bug.getBug(), e.getValue()));
            return checkbox;
        }), QBug.bug.solved, Messages.SOLVED);
        bugs.addOnClickNavigation(ReportTab.class, bug -> bug.getBug().getId());
        getContent().removeAll();
        getContent().add(bugs);
        getContent().setFlexGrow(1, bugs);
        getContent().setSizeFull();
    }
}
