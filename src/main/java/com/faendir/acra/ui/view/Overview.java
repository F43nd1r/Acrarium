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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.User;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ConfigurationLabel;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.base.popup.ValidatedField;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.faendir.acra.util.ImportResult;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@Route(value = "", layout = MainView.class)
public class Overview extends VerticalLayout implements ComponentEventListener<AttachEvent>, HasRoute {
    private final DataService dataService;

    @Autowired
    public Overview(DataService dataService) {
        this.dataService = dataService;
        addAttachListener(this);
    }

    @Override
    public void onComponentEvent(AttachEvent event) {
        removeAll();
        MyGrid<VApp> grid = new MyGrid<>(dataService.getAppProvider());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        Grid.Column<VApp> appColumn = grid.addColumn(VApp::getName, QApp.app.name, Messages.NAME).setFlexGrow(1);
        grid.addColumn(VApp::getBugCount, QBug.bug.countDistinct(), Messages.BUGS);
        grid.addColumn(VApp::getReportCount, QReport.report.count(), Messages.REPORTS);
        grid.addOnClickNavigation(BugTab.class, VApp::getId);
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            grid.appendFooterRow().getCell(appColumn).setComponent(new Div(Translatable.createButton(e -> {
                Translatable<TextField> name = Translatable.createTextField("", Messages.NAME);
                new Popup().setTitle(Messages.NEW_APP).addComponent(name).addCreateButton(popup -> {
                    popup.clear().addComponent(new ConfigurationLabel(dataService.createNewApp(name.getContent().getValue()))).addCloseButton().show();
                    grid.getDataProvider().refreshAll();
                }).show();
            }, Messages.NEW_APP), Translatable.createButton(e -> {
                Translatable<TextField> host = Translatable.createTextField("localhost", Messages.HOST);
                NumberField port = new NumberField();
                port.setValue(5984d);
                port.setMin(0);
                port.setMax(65535);
                Translatable<Checkbox> ssl = Translatable.createCheckbox(false, Messages.SSL);
                Translatable.Value<TextField> databaseName = Translatable.createTextField("acra-myapp", Messages.DATABASE_NAME);
                new Popup().setTitle(Messages.IMPORT_ACRALYZER)
                        .addComponent(host)
                        .addComponent(port)
                        .addComponent(ssl)
                        .addValidatedField(ValidatedField.of(databaseName), true)
                        .addCreateButton(popup -> {
                            ImportResult importResult = dataService.importFromAcraStorage(host.getContent().getValue(), port.getValue().intValue(), ssl.getContent().getValue(), databaseName.getContent().getValue());
                            popup.clear()
                                    .addComponent(Translatable.createLabel(Messages.IMPORT_SUCCESS, importResult.getSuccessCount(), importResult.getTotalCount()))
                                    .addComponent(new ConfigurationLabel(importResult.getUser()))
                                    .addCloseButton()
                                    .show();
                            grid.getDataProvider().refreshAll();
                        })
                        .show();
            }, Messages.IMPORT_ACRALYZER)));
        }
        setSizeFull();
        add(grid);
    }

    @Override
    @NonNull
    public Path.Element<?> getPathElement() {
        return new Path.ImageElement<>(getClass(), "frontend/logo.png", Messages.OVERVIEW);
    }
}
