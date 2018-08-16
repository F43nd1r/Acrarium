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

import com.faendir.acra.i18n.HasI18n;
import com.vaadin.ui.Composite;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.AcraTheme;
import org.jfree.chart.JFreeChart;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.vaadin.addon.JFreeChartWrapper;
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.support.Translatable;

import java.awt.*;
import java.util.Locale;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
abstract class Chart<T> extends Composite implements Translatable, HasI18n {
    private final Panel panel;
    private final I18N i18n;
    private final String captionId;
    private final Object[] params;
    private JFreeChart chart;

    Chart(I18N i18n, String captionId, Object... params) {
        this.i18n = i18n;
        this.captionId = captionId;
        this.params = params;
        panel = new Panel();
        panel.addStyleName(AcraTheme.NO_BACKGROUND);
        setCompositionRoot(panel);
        updateMessageStrings(i18n.getLocale());
    }

    @Override
    public void updateMessageStrings(Locale locale) {
        setCaption(i18n.get(captionId, locale, params));
    }

    public void setContent(@NonNull Map<T, Long> map) {
        chart = createChart(map);
        JFreeChartWrapper content = new JFreeChartWrapper(chart);
        content.setWidth(100, Unit.PERCENTAGE);
        content.setHeight(100, Unit.PERCENTAGE);
        panel.setContent(content);
    }

    Paint getForegroundColor() {
        return UI.getCurrent().getTheme().toLowerCase().contains("dark") ? Statistics.FOREGROUND_DARK : Statistics.FOREGROUND_LIGHT;
    }

    protected abstract JFreeChart createChart(@NonNull Map<T, Long> map);

    @Nullable
    JFreeChart getChart() {
        return chart;
    }

    @Override
    public I18N getI18n() {
        return i18n;
    }
}
