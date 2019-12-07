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
package com.faendir.acra.ui.component.statistics;

import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.util.LocalSettings;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.sql.SQLExpressions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * @author lukas
 * @since 01.06.18
 */
class Property<F, C extends Component & HasValue<?, F> & HasEnabled & HasSize & HasStyle, T> {
    private final App app;
    private final DataService dataService;
    private final Translatable<Checkbox> checkBox;
    private final C filterComponent;
    private final Function<F, BooleanExpression> filter;
    private final Chart<T> chart;
    private final Expression<T> select;

    private Property(App app, C filterComponent, Function<F, BooleanExpression> filter, Chart<T> chart, DataService dataService, Expression<T> select, String filterTextId, Object... params) {
        this.app = app;
        this.dataService = dataService;
        this.checkBox = Translatable.createCheckbox(false, filterTextId, params);
        checkBox.preventWhiteSpaceBreaking();
        this.filterComponent = filterComponent;
        this.filter = filter;
        this.chart = chart;
        this.select = select;
        filterComponent.setEnabled(false);
        filterComponent.setWidth("100%");
        checkBox.getContent().addValueChangeListener(e -> filterComponent.setEnabled(e.getValue()));
    }

    void addTo(FormLayout filterLayout, FlexComponent chartLayout) {
        filterLayout.addFormItem(filterComponent, checkBox);
        chartLayout.add(chart);
        chartLayout.expand(chart);
    }

    BooleanExpression applyFilter(@Nullable BooleanExpression expression) {
        if (checkBox.getContent().getValue() && filterComponent.getValue() != null) {
            return filter.apply(filterComponent.getValue()).and(expression);
        }
        return expression;
    }

    void update(BooleanExpression expression) {
        chart.setContent(dataService.countReports(app, expression, select));
    }

    static class Factory {
        private final DataService dataService;
        private final BooleanExpression expression;

        Factory(DataService dataService, BooleanExpression expression) {
            this.dataService = dataService;
            this.expression = expression;
        }

        Property<?, ?, ?> createStringProperty(@NonNull LocalSettings localSettings, App app, ComparableExpressionBase<String> stringExpression, String filterTextId, String chartTitleId) {
            List<String> list = dataService.getFromReports(app, expression, stringExpression);
            Select<String> select = new Select<>(list.toArray(new String[0]));
            if(!list.isEmpty()) {
                select.setValue(list.get(0));
            }
            return new Property<>(app, select, stringExpression::eq, new PieChart(localSettings, chartTitleId), dataService, stringExpression, filterTextId);
        }

        Property<?, ?, ?> createAgeProperty(@NonNull LocalSettings localSettings, App app, DateTimePath<ZonedDateTime> dateTimeExpression, String filterTextId, String chartTitleId) {
            TextField stepper = new TextField();
            stepper.setValue("30");
            return new Property<>(app, stepper,
                    days -> dateTimeExpression.after(ZonedDateTime.now().minus(Integer.parseInt(days), ChronoUnit.DAYS)),
                    new TimeChart(localSettings, chartTitleId),
                    dataService,
                    SQLExpressions.date(Date.class, dateTimeExpression),
                    filterTextId);
        }
    }
}
