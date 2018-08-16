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

import com.faendir.acra.i18n.I18nCheckBox;
import com.faendir.acra.service.DataService;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.sql.SQLExpressions;
import com.vaadin.data.HasValue;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import org.vaadin.risto.stepper.IntStepper;
import org.vaadin.spring.i18n.I18N;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

/**
 * @author lukas
 * @since 01.06.18
 */
class Property<F, C extends Component & HasValue<F>, T> {
    private final DataService dataService;
    private final CheckBox checkBox;
    private final C filterComponent;
    private final Function<F, BooleanExpression> filter;
    private final Chart<T> chart;
    private final Expression<T> select;

    private Property(C filterComponent, Function<F, BooleanExpression> filter, Chart<T> chart, DataService dataService, Expression<T> select, I18N i18n, String filterTextId) {
        this.dataService = dataService;
        this.checkBox = new I18nCheckBox(i18n, filterTextId);
        this.filterComponent = filterComponent;
        this.filter = filter;
        this.chart = chart;
        this.select = select;
        filterComponent.setEnabled(false);
        filterComponent.setWidth(100, Sizeable.Unit.PERCENTAGE);
        checkBox.setValue(false);
        checkBox.addValueChangeListener(e -> filterComponent.setEnabled(e.getValue()));
    }

    void addTo(ComponentContainer filterLayout, ComponentContainer chartLayout) {
        filterLayout.addComponent(checkBox);
        filterLayout.addComponent(filterComponent);
        chartLayout.addComponent(chart);
    }

    BooleanExpression applyFilter(BooleanExpression expression) {
        if (checkBox.getValue() && filterComponent.getValue() != null) {
            return expression.and(filter.apply(filterComponent.getValue()));
        }
        return expression;
    }

    void update(BooleanExpression expression) {
        chart.setContent(dataService.countReports(expression, select));
    }

    static class Factory {
        private final DataService dataService;
        private final BooleanExpression expression;

        Factory(DataService dataService, BooleanExpression expression) {
            this.dataService = dataService;
            this.expression = expression;
        }

        Property<?, ?, ?> createStringProperty(ComparableExpressionBase<String> stringExpression, I18N i18n, String filterTextId, String chartTitleId) {
            ComboBox<String> comboBox = new ComboBox<>(null, dataService.getFromReports(expression, stringExpression));
            comboBox.setEmptySelectionAllowed(false);
            return new Property<>(comboBox, stringExpression::eq, new PieChart(i18n, chartTitleId), dataService, stringExpression, i18n, filterTextId);
        }

        Property<?, ?, ?> createAgeProperty(DateTimePath<ZonedDateTime> dateTimeExpression, I18N i18n, String filterTextId, String chartTitleId) {
            IntStepper stepper = new IntStepper();
            stepper.setValue(30);
            stepper.setMinValue(1);
            return new Property<>(stepper,
                    days -> dateTimeExpression.after(ZonedDateTime.now().minus(days, ChronoUnit.DAYS)),
                    new TimeChart(i18n, chartTitleId),
                    dataService,
                    SQLExpressions.date(Date.class, dateTimeExpression),
                    i18n,
                    filterTextId);
        }
    }
}
