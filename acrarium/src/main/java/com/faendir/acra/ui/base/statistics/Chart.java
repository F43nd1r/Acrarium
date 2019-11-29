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

import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.util.LocalSettings;
import com.github.appreciated.apexcharts.ApexCharts;
import com.vaadin.flow.component.Composite;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * @author lukas
 * @since 01.06.18
 */
abstract class Chart<T> extends Composite<Card> {
    private final LocalSettings localSettings;

    Chart(@NonNull LocalSettings localSettings, @NonNull String captionId, @NonNull Object... params) {
        this.localSettings = localSettings;
        getContent().setWidth(500, HasSize.Unit.PIXEL);
        getContent().setMaxWidthFull();
        getContent().setHeader(Translatable.createLabel(captionId, params));
    }

    public void setContent(@NonNull Map<T, Long> map) {
        getContent().removeAll();
        getContent().add(createChart(map));
    }

    boolean isDarkTheme() {
        return localSettings.getDarkTheme();
    }

    protected abstract ApexCharts createChart(@NonNull Map<T, Long> map);
}
