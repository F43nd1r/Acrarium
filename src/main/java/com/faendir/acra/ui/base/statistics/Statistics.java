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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.Card;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
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
        filterLayout.setWidth("100%");
        Card card = new Card(filterLayout);
        card.setHeader(Translatable.createText(Messages.FILTER));
        card.setWidth(500, HasSize.Unit.PIXEL);

        TextField dayStepper = new TextField();
        dayStepper.setValue("30");
        Property.Factory factory = new Property.Factory(dataService, baseExpression);
        properties.add(factory.createAgeProperty(app, QReport.report.date, Messages.LAST_X_DAYS, Messages.REPORTS_OVER_TIME));
        properties.add(factory.createStringProperty(app, QReport.report.androidVersion, Messages.ANDROID_VERSION, Messages.REPORTS_PER_ANDROID_VERSION));
        properties.add(factory.createStringProperty(app, QReport.report.stacktrace.version.name, Messages.APP_VERSION, Messages.REPORTS_PER_APP_VERSION));
        properties.add(factory.createStringProperty(app, QReport.report.phoneModel, Messages.PHONE_MODEL, Messages.REPORTS_PER_PHONE_MODEL));
        properties.add(factory.createStringProperty(app, QReport.report.brand, Messages.PHONE_BRAND, Messages.REPORTS_PER_BRAND));

        getContent().setFlexWrap(FlexLayout.FLEX_WRAP.WRAP);
        getContent().setWidthFull();
        getContent().removeAll();

        getContent().add(card);
        getContent().expand(card);
        properties.forEach(property -> property.addTo(filterLayout, getContent()));

        filterLayout.add(Translatable.createButton(e -> update(), Messages.APPLY));
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
