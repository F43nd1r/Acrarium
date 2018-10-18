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

import com.faendir.acra.ui.base.Card;
import com.faendir.acra.ui.base.JFreeChartWrapper;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import org.jfree.chart.JFreeChart;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
abstract class Chart<T> extends Composite<Card> {
    private final String caption;
    private JFreeChart chart;

    Chart(String caption) {
        this.caption = caption;
        getContent().setWidth("500px");
        getContent().getStyle().set("max-width","100%");
        getContent().setHeader(new Text(caption));
    }

    public void setContent(@NonNull Map<T, Long> map) {
        chart = createChart(map);
        JFreeChartWrapper content = new JFreeChartWrapper(chart);
        content.setSvgAspectRatio("xMidYMid");
        content.setWidth("100%");
        getContent().removeAll();
        getContent().add(content);
    }

    Paint getForegroundColor() {
        return /*UI.getCurrent().getTheme().toLowerCase().contains("dark")*/false ? Statistics.FOREGROUND_DARK : Statistics.FOREGROUND_LIGHT;
    }

    protected abstract JFreeChart createChart(@NonNull Map<T, Long> map);

    @Nullable
    JFreeChart getChart() {
        return chart;
    }
}
