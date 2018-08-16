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

package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.statistics.Statistics;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 21.05.18
 */
@SpringComponent("bugStatisticsTab")
@ViewScope
public class StatisticsTab implements BugTab {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public StatisticsTab(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Panel root = new Panel(new Statistics(QReport.report.stacktrace.bug.id.eq(bug.getId()), dataService, i18n));
        root.setSizeFull();
        root.addStyleNames(AcraTheme.NO_BACKGROUND, AcraTheme.NO_BORDER);
        return root;
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.STATISTICS);
    }

    @Override
    public String getId() {
        return "statistics";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
