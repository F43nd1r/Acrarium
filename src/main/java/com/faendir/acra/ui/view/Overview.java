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

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nCheckBox;
import com.faendir.acra.i18n.I18nIntStepper;
import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.I18nTextField;
import com.faendir.acra.i18n.Messages;
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
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringComponent
@ViewScope
public class Overview extends BaseView {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;
    private MyGrid<VApp> grid;

    @Autowired
    public Overview(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>(dataService.getAppProvider(), i18n, Messages.APPS);
        grid.setResponsive(true);
        grid.setSizeToRows();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(VApp::getName, QApp.app.name, Messages.NAME);
        grid.addColumn(VApp::getBugCount, QBug.bug.countDistinct(), Messages.BUGS);
        grid.addColumn(VApp::getReportCount, QReport.report.count(), Messages.REPORTS);
        grid.addOnClickNavigation(getNavigationManager(), AppView.class, e -> String.valueOf(e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout();
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            Button add = new I18nButton(i18n, Messages.NEW_APP);
            add.addClickListener(e -> {
                TextField name = new I18nTextField(i18n, Messages.NAME);
                new Popup(i18n, Messages.NEW_APP).addComponent(name).addCreateButton(popup -> {
                    popup.clear().addComponent(new ConfigurationLabel(dataService.createNewApp(name.getValue()), i18n)).addCloseButton().show();
                    grid.getDataProvider().refreshAll();
                }).show();
            });
            Button importButton = new I18nButton(i18n, Messages.IMPORT_ACRALYZER);
            importButton.addClickListener(e -> {
                I18nTextField host = new I18nTextField("localhost", i18n, Messages.HOST);
                I18nIntStepper port = new I18nIntStepper(5984, i18n, Messages.PORT);
                port.setMinValue(0);
                port.setMaxValue(65535);
                I18nCheckBox ssl = new I18nCheckBox(i18n, Messages.SSL);
                I18nTextField databaseName = new I18nTextField("acra-myapp", i18n, Messages.DATABASE_NAME);
                new Popup(i18n, Messages.IMPORT_ACRALYZER).addValidatedField(ValidatedField.of(host), true)
                        .addValidatedField(ValidatedField.of(port), true)
                        .addValidatedField(ValidatedField.of(ssl), true)
                        .addValidatedField(ValidatedField.of(databaseName), true)
                        .addCreateButton(popup -> {
                            ImportResult importResult = dataService.importFromAcraStorage(host.getValue(), port.getValue(), ssl.getValue(), databaseName.getValue());
                            popup.clear()
                                    .addComponent(new I18nLabel(i18n, Messages.IMPORT_SUCCESS, importResult.getSuccessCount(), importResult.getTotalCount()))
                                    .addComponent(new ConfigurationLabel(importResult.getUser(), i18n))
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
        @NonNull private final I18N i18n;

        protected Provider(@NonNull I18N i18n) {
            super(Overview.class);
            this.i18n = i18n;
        }

        @Override
        public String getTitle(String parameter) {
            return i18n.get(Messages.ACRARIUM);
        }

        @Override
        public String getId() {
            return "";
        }
    }
}
