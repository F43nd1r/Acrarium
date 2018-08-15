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
package com.faendir.acra.ui.view;

import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.User;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.SingleViewProvider;
import com.faendir.acra.ui.view.app.AppView;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.faendir.acra.ui.view.base.navigation.BaseView;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.base.popup.ValidatedField;
import com.faendir.acra.util.ImportResult;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringComponent
@ViewScope
public class Overview extends BaseView {
    @NonNull private final DataService dataService;
    private MyGrid<VApp> grid;

    @Autowired
    public Overview(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", dataService.getAppProvider());
        grid.setResponsive(true);
        grid.setSizeToRows();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(VApp::getName, QApp.app.name, "Name");
        grid.addColumn(VApp::getBugCount, QBug.bug.countDistinct(), "Bugs");
        grid.addColumn(VApp::getReportCount, QReport.report.count(), "Reports");
        grid.addOnClickNavigation(getNavigationManager(), AppView.class, e -> String.valueOf(e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout();
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            Button add = new Button("New App", e -> {
                TextField name = new TextField("Name");
                new Popup().setTitle("New App").addComponent(name).addCreateButton(popup -> {
                    popup.clear().addComponent(new ConfigurationLabel(dataService.createNewApp(name.getValue()))).addCloseButton().show();
                    grid.getDataProvider().refreshAll();
                }).show();
            });
            Button importButton = new Button("Import from Acralyzer", e -> {
                TextField host = new TextField("Host", "localhost");
                IntStepper port = new IntStepper("Port");
                port.setValue(5984);
                port.setMinValue(0);
                port.setMaxValue(65535);
                CheckBox ssl = new CheckBox("Use ssl", false);
                TextField databaseName = new TextField("Database Name", "acra-myapp");
                new Popup().setTitle("Import from Acralyzer")
                        .addValidatedField(ValidatedField.of(host), true)
                        .addValidatedField(ValidatedField.of(port), true)
                        .addValidatedField(ValidatedField.of(ssl), true)
                        .addValidatedField(ValidatedField.of(databaseName), true)
                        .addCreateButton(popup -> {
                            ImportResult importResult = dataService.importFromAcraStorage(host.getValue(), port.getValue(), ssl.getValue(), databaseName.getValue());
                            popup.clear()
                                    .addComponent(new Label(String.format("Import successful for %d of %d reports.", importResult.getSuccessCount(), importResult.getTotalCount())))
                                    .addComponent(new ConfigurationLabel(importResult.getUser()))
                                    .addCloseButton()
                                    .show();
                            grid.getDataProvider().refreshAll();
                        })
                        .show();
            });
            layout.addComponent(new HorizontalLayout(add, importButton));
        }
        layout.addComponent(grid);
        layout.addStyleNames(AcraTheme.NO_PADDING, AcraTheme.PADDING_LEFT, AcraTheme.PADDING_RIGHT, AcraTheme.PADDING_BOTTOM, AcraTheme.MAX_WIDTH_728);
        VerticalLayout root = new VerticalLayout(layout);
        root.setSpacing(false);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.TOP_CENTER);
        root.addStyleName(AcraTheme.NO_PADDING);
        setCompositionRoot(root);
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleViewProvider<Overview> {
        protected Provider() {
            super(Overview.class);
        }

        @Override
        public String getTitle(String parameter) {
            return "Acrarium";
        }

        @Override
        public String getId() {
            return "";
        }
    }
}
