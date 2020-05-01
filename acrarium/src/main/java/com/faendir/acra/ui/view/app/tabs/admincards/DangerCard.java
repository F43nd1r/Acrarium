/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.view.app.tabs.admincards;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.*;
import com.faendir.acra.ui.component.dialog.FluentDialog;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import static com.faendir.acra.model.QReport.report;

@UIScope
@SpringComponent
public class DangerCard extends AdminCard {
    public DangerCard(DataService dataService) {
        super(dataService);
        setHeader(Translatable.createLabel(Messages.DANGER_ZONE));
        enableDivider();
        setHeaderColor("var(--lumo-error-contrast-color)", "var(--lumo-error-color)");
    }

    @Override
    public void init(App app) {
        removeContent();
        Box configBox = new Box(Translatable.createLabel(Messages.NEW_ACRA_CONFIG), Translatable.createLabel(Messages.NEW_ACRA_CONFIG_DETAILS),
                Translatable.createButton(e -> new FluentDialog().addText(Messages.NEW_ACRA_CONFIG_CONFIRM)
                        .addConfirmButtons(popup -> new FluentDialog().addComponent(new ConfigurationLabel(getDataService().recreateReporterUser(app))).addCloseButton().show())
                        .show(), Messages.CREATE));
        Box matchingBox = new Box(Translatable.createLabel(Messages.NEW_BUG_CONFIG), Translatable.createLabel(Messages.NEW_BUG_CONFIG_DETAILS),
                Translatable.createButton(e -> {
                    App.Configuration configuration = app.getConfiguration();
                    Translatable.ValidatedValue<RangeField, ?, Double> score = Translatable.createRangeField(Messages.SCORE).with(it -> {
                        it.setMin(0);
                        it.setMax(100);
                        it.setValue((double) configuration.getMinScore());
                    });
                    new FluentDialog().addComponent(score)
                            .addText(Messages.NEW_BUG_CONFIG_CONFIRM)
                            .addConfirmButtons(p -> getDataService().changeConfiguration(app, new App.Configuration(score.getValue().intValue())))
                            .show();
                }, Messages.CONFIGURE));
        Box purgeAgeBox = new Box(Translatable.createLabel(Messages.PURGE_OLD), Translatable.createLabel(Messages.PURGE_OLD_DETAILS), Translatable.createButton(e -> {
            Translatable.ValidatedValue<NumberField, ?, Double> age = Translatable.createNumberField(30d, Messages.REPORTS_OLDER_THAN1).with(it -> {
                        it.setStep(1d);
                        it.setMin(1d);
                        it.setPreventInvalidInput(true);
                        it.setHasControls(true);
                        it.setWidthFull();
                        it.setSuffixComponent(Translatable.createLabel(Messages.REPORTS_OLDER_THAN2));
                    }
            );
            new FluentDialog().addComponent(age)
                    .setTitle(Messages.PURGE)
                    .addConfirmButtons(popup -> {
                        getDataService().deleteReportsOlderThanDays(app, age.getValue().intValue());
                    }).show();
        }, Messages.PURGE));
        Box purgeVersionBox = new Box(Translatable.createLabel(Messages.PURGE_VERSION), Translatable.createLabel(Messages.PURGE_VERSION_DETAILS), Translatable.createButton(e -> {
            Translatable.ValidatedValue<ComboBox<Integer>, ?, Integer> versionBox = Translatable.createComboBox(getDataService().getFromReports(app, null, report.stacktrace.version.code), Messages.REPORTS_BEFORE_VERSION);
            new FluentDialog().addComponent(versionBox)
                    .setTitle(Messages.PURGE)
                    .addConfirmButtons(popup -> {
                        if (versionBox.getValue() != null) {
                            getDataService().deleteReportsBeforeVersion(app, versionBox.getValue());
                        }
                    }).show();
        }, Messages.PURGE));
        Box deleteBox = new Box(Translatable.createLabel(Messages.DELETE_APP), Translatable.createLabel(Messages.DELETE_APP_DETAILS), Translatable.createButton(e ->
                new FluentDialog().addText(Messages.DELETE_APP_CONFIRM).addConfirmButtons(popup -> {
                    getDataService().delete(app);
                    UI.getCurrent().navigate(Overview.class);
                }).show(), Messages.DELETE));
        add(configBox, matchingBox, purgeAgeBox, purgeVersionBox, deleteBox);
    }
}
