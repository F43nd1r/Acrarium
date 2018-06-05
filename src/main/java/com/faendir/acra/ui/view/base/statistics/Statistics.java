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

import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.view.base.FlexLayout;
import com.faendir.acra.util.Style;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import org.vaadin.risto.stepper.IntStepper;

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
    private final BooleanExpression baseExpression;
    private final List<Property<?, ?, ?>> properties;

    public Statistics(BooleanExpression baseExpression, DataService dataService) {
        this.baseExpression = baseExpression;
        properties = new ArrayList<>();
        GridLayout filterLayout = new GridLayout(2, 1);
        filterLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        filterLayout.setSpacing(true);
        filterLayout.setSizeFull();
        Style.apply(filterLayout, Style.PADDING_LEFT, Style.PADDING_TOP, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        filterLayout.setColumnExpandRatio(1, 1);

        IntStepper dayStepper = new IntStepper();
        dayStepper.setValue(30);
        dayStepper.setMinValue(1);
        Property.Factory factory = new Property.Factory(dataService, baseExpression);
        properties.add(factory.createAgeProperty("Last X days", "Reports over time", QReport.report.date));
        properties.add(factory.createStringProperty("Android Version", "Reports per Android Version", QReport.report.androidVersion));
        properties.add(factory.createStringProperty("App Version", "Reports per App Version", QReport.report.stacktrace.versionName));
        properties.add(factory.createStringProperty("Phone Model", "Reports per Phone Model", QReport.report.phoneModel));
        properties.add(factory.createStringProperty("Phone Brand", "Reports per Brand", QReport.report.brand));

        Panel filterPanel = new Panel(filterLayout);
        Style.NO_BACKGROUND.apply(filterPanel);
        filterPanel.setCaption("Filter");
        FlexLayout layout = new FlexLayout(filterPanel);
        layout.setWidth(100, Unit.PERCENTAGE);

        properties.forEach(property -> property.addTo(filterLayout, layout));

        Button applyButton = new Button("Apply", e -> update());
        applyButton.setWidth(100, Unit.PERCENTAGE);
        filterLayout.space();
        filterLayout.addComponent(applyButton);
        filterLayout.newLine();

        Panel root = new Panel(layout);
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
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
