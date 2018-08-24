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

package com.faendir.acra.ui.view.base.statistics;

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nPanel;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.view.base.layout.FlexLayout;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.lang.Nullable;
import org.vaadin.risto.stepper.IntStepper;
import org.vaadin.spring.i18n.I18N;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * @since 21.05.18
 */
public class Statistics extends Composite {
    static final Color BLUE = new Color(0x197de1); //vaadin blue
    static final Color FOREGROUND_DARK = new Color(0xcacecf);
    static final Color FOREGROUND_LIGHT = new Color(0x464646);
    @Nullable private final BooleanExpression baseExpression;
    private final List<Property<?, ?, ?>> properties;

    public Statistics(App app, @Nullable BooleanExpression baseExpression, DataService dataService, I18N i18n) {
        this.baseExpression = baseExpression;
        properties = new ArrayList<>();
        GridLayout filterLayout = new GridLayout(2, 1);
        filterLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        filterLayout.setSpacing(true);
        filterLayout.setSizeFull();
        filterLayout.addStyleNames(AcraTheme.PADDING_LEFT, AcraTheme.PADDING_TOP, AcraTheme.PADDING_RIGHT, AcraTheme.PADDING_BOTTOM);
        filterLayout.setColumnExpandRatio(1, 1);

        IntStepper dayStepper = new IntStepper();
        dayStepper.setValue(30);
        dayStepper.setMinValue(1);
        Property.Factory factory = new Property.Factory(dataService, baseExpression);
        properties.add(factory.createAgeProperty(app, QReport.report.date, i18n, Messages.LAST_X_DAYS, Messages.REPORTS_OVER_TIME));
        properties.add(factory.createStringProperty(app, QReport.report.androidVersion, i18n, Messages.ANDROID_VERSION, Messages.REPORTS_PER_ANDROID_VERSION));
        properties.add(factory.createStringProperty(app, QReport.report.stacktrace.version.name, i18n, Messages.APP_VERSION, Messages.REPORTS_PER_APP_VERSION));
        properties.add(factory.createStringProperty(app, QReport.report.phoneModel, i18n, Messages.PHONE_MODEL, Messages.REPORTS_PER_PHONE_MODEL));
        properties.add(factory.createStringProperty(app, QReport.report.brand, i18n, Messages.PHONE_BRAND, Messages.REPORTS_PER_BRAND));

        Panel filterPanel = new I18nPanel(filterLayout, i18n, Messages.FILTER);
        filterPanel.addStyleName(AcraTheme.NO_BACKGROUND);
        FlexLayout layout = new FlexLayout(filterPanel);
        layout.setWidth(100, Unit.PERCENTAGE);

        properties.forEach(property -> property.addTo(filterLayout, layout));

        Button applyButton = new I18nButton(e -> update(), i18n, Messages.APPLY);
        applyButton.setWidth(100, Unit.PERCENTAGE);
        filterLayout.space();
        filterLayout.addComponent(applyButton);
        filterLayout.newLine();

        Panel root = new Panel(layout);
        root.setSizeFull();
        root.addStyleNames(AcraTheme.NO_BACKGROUND, AcraTheme.NO_BORDER);
        setCompositionRoot(root);
        update();
    }

    private void update() {
        BooleanExpression expression = baseExpression;
        for (Property<?, ?, ?> property : properties) {
            expression = property.applyFilter(expression);
        }
        for (Property<?, ?, ?> property : properties) {
            property.update(expression);
        }
    }
}
