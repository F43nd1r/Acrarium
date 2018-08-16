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

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.I18nSlider;
import com.faendir.acra.i18n.Messages;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class DangerPanel implements AdminPanel {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public DangerPanel(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Button configButton = new I18nButton(e -> new Popup(i18n, Messages.CONFIRM).addComponent(new I18nLabel(ContentMode.HTML, i18n, Messages.NEW_ACRA_CONFIG_CONFIRM))
                .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(dataService.recreateReporterUser(app), i18n)).addCloseButton().show())
                .show(), i18n, Messages.NEW_ACRA_CONFIG);
        configButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button matchingButton = new I18nButton(e -> {
            App.Configuration configuration = app.getConfiguration();
            I18nSlider score = new I18nSlider(0, 100, i18n, Messages.MIN_SCORE);
            score.setValue((double) configuration.getMinScore());
            new Popup(i18n, Messages.CONFIRM).addValidatedField(ValidatedField.of(score), true)
                    .addComponent(new I18nLabel(i18n, Messages.NEW_BUG_CONFIG_CONFIRM))
                    .addYesNoButtons(p -> dataService.changeConfiguration(app, new App.Configuration(score.getValue().intValue())), true)
                    .show();
        }, i18n, Messages.NEW_BUG_CONFIG);
        matchingButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        IntStepper age = new IntStepper();
        age.setValue(30);
        age.setMinValue(0);
        age.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeAge = new HorizontalLayout();
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        purgeAge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        purgeAge.addStyleName(AcraTheme.NO_MARGIN);
        purgeAge.addComponents(new I18nButton(e -> dataService.deleteReportsOlderThanDays(app, age.getValue()), i18n, Messages.PURGE),
                new I18nLabel(i18n, Messages.REPORTS_OLDER_THAN1),
                age,
                new I18nLabel(i18n, Messages.REPORTS_OLDER_THAN2));
        purgeAge.setExpandRatio(age, 1);
        ComboBox<Integer> versionBox = new ComboBox<>(null, dataService.getFromReports(QReport.report.stacktrace.bug.app.eq(app), QReport.report.stacktrace.version.code));
        versionBox.setEmptySelectionAllowed(false);
        versionBox.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeVersion = new HorizontalLayout();
        purgeVersion.setWidth(100, Sizeable.Unit.PERCENTAGE);
        purgeVersion.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        purgeVersion.addStyleName(AcraTheme.NO_MARGIN);
        purgeVersion.addComponents(new I18nButton(e -> {
            if (versionBox.getValue() != null) {
                dataService.deleteReportsBeforeVersion(app, versionBox.getValue());
            }
        }, i18n, Messages.PURGE), new I18nLabel(i18n, Messages.REPORTS_BEFORE_VERSION), versionBox);
        purgeVersion.setExpandRatio(versionBox, 1);
        Button deleteButton = new I18nButton(e -> new Popup(i18n, Messages.CONFIRM).addComponent(new I18nLabel(i18n, Messages.DELETE_APP_CONFIRM)).addYesNoButtons(popup -> {
            dataService.delete(app);
            navigationManager.navigateBack();
        }, true).show(), i18n, Messages.DELETE_APP);
        VerticalLayout layout = new VerticalLayout(configButton, matchingButton, purgeAge, purgeVersion, deleteButton);
        deleteButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        layout.setSizeFull();
        layout.addStyleName(AcraTheme.NO_PADDING);
        return layout;
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.DANGER_ZONE);
    }

    @Override
    public String getId() {
        return "danger-zone";
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
