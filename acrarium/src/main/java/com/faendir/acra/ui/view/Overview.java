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
import com.faendir.acra.ui.component.ConfigurationLabel;
import com.faendir.acra.ui.base.HasAcrariumTitle;
import com.faendir.acra.ui.component.grid.AcrariumGrid;
import com.faendir.acra.ui.base.TranslatableText;
import com.faendir.acra.ui.component.dialog.FluentDialog;
import com.faendir.acra.ui.component.dialog.ValidatedField;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.faendir.acra.util.ImportResult;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@Route(value = "", layout = MainView.class)
public class Overview extends VerticalLayout implements ComponentEventListener<AttachEvent>, HasAcrariumTitle {
    private final DataService dataService;

    @Autowired
    public Overview(DataService dataService) {
        this.dataService = dataService;
        addAttachListener(this);
    }

    @Override
    public void onComponentEvent(AttachEvent event) {
        removeAll();
        AcrariumGrid<VApp> grid = new AcrariumGrid<>(dataService.getAppProvider());
        grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.NONE);
        com.vaadin.flow.component.grid.Grid.Column<VApp> appColumn = grid.addColumn(VApp::getName).setSortable( QApp.app.name).setCaption(Messages.NAME).setFlexGrow(1);
        grid.addColumn(VApp::getBugCount).setSortable(QBug.bug.countDistinct()).setCaption(Messages.BUGS);
        grid.addColumn(VApp::getReportCount).setSortable(QReport.report.count()).setCaption(Messages.REPORTS);
        grid.addOnClickNavigation(BugTab.class, VApp::getId);
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            grid.appendFooterRow().getCell(appColumn).setComponent(new HorizontalLayout(Translatable.createButton(e -> {
                Translatable<TextField> name = Translatable.createTextField("", Messages.NAME);
                new FluentDialog().setTitle(Messages.NEW_APP).addComponent(name).addCreateButton(popup -> {
                    new FluentDialog().addComponent(new ConfigurationLabel(dataService.createNewApp(name.getContent().getValue()))).addCloseButton().show();
                    grid.getDataProvider().refreshAll();
                }).show();
            }, Messages.NEW_APP), Translatable.createButton(e -> {
                Translatable<TextField> host = Translatable.createTextField("localhost", Messages.HOST);
                Translatable.ValidatedValue<NumberField, ?, Double> port = Translatable.createNumberField(5984, Messages.PORT).with(p -> {
                    p.setMin(0);
                    p.setMax(65535);
                });
                Translatable<Checkbox> ssl = Translatable.createCheckbox(false, Messages.SSL);
                Translatable.ValidatedValue<TextField, ?, String> databaseName = Translatable.createTextField("acra-myapp", Messages.DATABASE_NAME);
                new FluentDialog().setTitle(Messages.IMPORT_ACRALYZER)
                        .addComponent(host)
                        .addComponent(port)
                        .addComponent(ssl)
                        .addValidatedField(ValidatedField.of(databaseName), true)
                        .addCreateButton(popup -> {
                            ImportResult importResult = dataService.importFromAcraStorage(host.getContent().getValue(), port.getValue().intValue(), ssl.getContent().getValue(), databaseName.getContent().getValue());
                            new FluentDialog().addComponent(Translatable.createLabel(Messages.IMPORT_SUCCESS, importResult.getSuccessCount(), importResult.getTotalCount()))
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
    public TranslatableText getTitle() {
        return new TranslatableText(Messages.ACRARIUM);
    }
}
