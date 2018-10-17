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

package com.faendir.acra.ui.base.statistics;

import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * @since 21.05.18
 */
public class Statistics extends Composite<FlexLayout> {
    static final Color BLUE = new Color(0x197de1); //vaadin blue
    static final Color FOREGROUND_DARK = new Color(0xcacecf);
    static final Color FOREGROUND_LIGHT = new Color(0x464646);
    static final Font LABEL_FONT = new Font("Roboto", Font.PLAIN, 18);
    @Nullable
    private final BooleanExpression baseExpression;
    private final List<Property<?, ?, ?>> properties;

    public Statistics(App app, @Nullable BooleanExpression baseExpression, DataService dataService) {
        this.baseExpression = baseExpression;
        properties = new ArrayList<>();
        FormLayout filterLayout = new FormLayout();
        filterLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1));
        filterLayout.setWidth("500px");

        TextField dayStepper = new TextField();
        dayStepper.setValue("30");
        Property.Factory factory = new Property.Factory(dataService, baseExpression);
        properties.add(factory.createAgeProperty(app, QReport.report.date, "Last X Days", "Reports over time"));
        properties.add(factory.createStringProperty(app, QReport.report.androidVersion, "Android Version", "Reports per Android Version"));
        properties.add(factory.createStringProperty(app, QReport.report.stacktrace.version.name, "App Version", "Reports per App Version"));
        properties.add(factory.createStringProperty(app, QReport.report.phoneModel, "Phone Model", "Reports per Phone Model"));
        properties.add(factory.createStringProperty(app, QReport.report.brand, "Brand", "Reports per Brand"));

        getContent().getStyle().set("flex-wrap","wrap");
        getContent().setWidth("100%");
        getContent().removeAll();

        getContent().add(filterLayout);
        getContent().expand(filterLayout);
        properties.forEach(property -> property.addTo(filterLayout, getContent()));

        Button applyButton = new Button("Apply", e -> update());
        filterLayout.add(applyButton);
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
