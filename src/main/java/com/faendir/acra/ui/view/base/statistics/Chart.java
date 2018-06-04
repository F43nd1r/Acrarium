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

import com.faendir.acra.util.Style;
import com.vaadin.ui.Composite;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import org.jfree.chart.JFreeChart;
import org.springframework.lang.NonNull;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
abstract class Chart<T> extends Composite {
    private final Panel panel;

    Chart(@NonNull String caption) {
        panel = new Panel();
        panel.setCaption(caption);
        Style.NO_BACKGROUND.apply(panel);
        setCompositionRoot(panel);
    }

    public void setContent(@NonNull Map<T, Long> map) {
        JFreeChart chart = createChart(map);
        JFreeChartWrapper content = new JFreeChartWrapper(chart);
        content.setWidth(100, Unit.PERCENTAGE);
        content.setHeight(100, Unit.PERCENTAGE);
        panel.setContent(content);
    }

    Paint getForegroundColor() {
        return UI.getCurrent().getTheme().toLowerCase().contains("dark") ? Statistics.FOREGROUND_DARK : Statistics.FOREGROUND_LIGHT;
    }

    protected abstract JFreeChart createChart(@NonNull Map<T, Long> map);
}
