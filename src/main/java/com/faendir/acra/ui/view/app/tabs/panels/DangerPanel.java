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
package com.faendir.acra.ui.view.app.tabs.panels;

import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.base.popup.ValidatedField;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class DangerPanel implements AdminPanel {
    @NonNull private final DataService dataService;

    @Autowired
    public DangerPanel(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Button configButton = new Button("Create new ACRA Configuration",
                e -> new Popup().setTitle("Confirm")
                        .addComponent(new Label("Are you sure you want to create a new ACRA configuration?<br>The existing configuration will be invalidated", ContentMode.HTML))
                        .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(dataService.recreateReporterUser(app))).addCloseButton().show())
                        .show());
        configButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button matchingButton = new Button("Configure bug matching", e -> {
            App.Configuration configuration = app.getConfiguration();
            CheckBox matchByMessage = new CheckBox("Match by exception message", configuration.matchByMessage());
            CheckBox ignoreInstanceIds = new CheckBox("Ignore instance ids", configuration.ignoreInstanceIds());
            CheckBox ignoreAndroidLineNumbers = new CheckBox("Ignore android SDK line numbers", configuration.ignoreAndroidLineNumbers());
            new Popup().addValidatedField(ValidatedField.of(matchByMessage), true)
                    .addValidatedField(ValidatedField.of(ignoreInstanceIds), true)
                    .addValidatedField(ValidatedField.of(ignoreAndroidLineNumbers), true)
                    .addComponent(new Label(
                            "Are you sure you want to save this configuration? All bugs will be recalculated, which may take some time and will reset the 'solved' status"))
                    .addYesNoButtons(p -> dataService.changeConfiguration(app,
                            new App.Configuration(matchByMessage.getValue(), ignoreInstanceIds.getValue(), ignoreAndroidLineNumbers.getValue())), true)
                    .show();
        });
        matchingButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        IntStepper age = new IntStepper();
        age.setValue(30);
        age.setMinValue(0);
        age.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeAge = new HorizontalLayout();
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        purgeAge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        purgeAge.addStyleName(AcraTheme.NO_MARGIN);
        purgeAge.addComponents(new Button("Purge", e -> dataService.deleteReportsOlderThanDays(app, age.getValue())), new Label(" Reports older than "), age, new Label(" Days"));
        purgeAge.setExpandRatio(age, 1);
        ComboBox<Integer> versionBox = new ComboBox<>(null, dataService.getFromReports(QReport.report.stacktrace.bug.app.eq(app), QReport.report.stacktrace.versionCode));
        versionBox.setEmptySelectionAllowed(false);
        versionBox.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeVersion = new HorizontalLayout();
        purgeVersion.setWidth(100, Sizeable.Unit.PERCENTAGE);
        purgeVersion.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        purgeVersion.addStyleName(AcraTheme.NO_MARGIN);
        purgeVersion.addComponents(new Button("Purge", e -> {
            if (versionBox.getValue() != null) {
                dataService.deleteReportsBeforeVersion(app, versionBox.getValue());
            }
        }), new Label(" Reports before Version "), versionBox);
        purgeVersion.setExpandRatio(versionBox, 1);
        Button deleteButton = new Button("Delete App",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to delete this app and all its associated content?")).addYesNoButtons(popup -> {
                    dataService.delete(app);
                    navigationManager.navigateBack();
                }, true).show());
        VerticalLayout layout = new VerticalLayout(configButton, matchingButton, purgeAge, purgeVersion, deleteButton);
        deleteButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        layout.setSizeFull();
        layout.addStyleName(AcraTheme.NO_PADDING);
        return layout;
    }

    @Override
    public String getCaption() {
        return "Danger Zone";
    }

    @Override
    public Resource getIcon() {
        return VaadinIcons.EXCLAMATION;
    }

    @Override
    public String getStyleName() {
        return AcraTheme.RED_PANEL_HEADER;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
