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

import com.faendir.acra.model.Bug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.statistics.Statistics;
import com.faendir.acra.util.Style;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 21.05.18
 */
@SpringComponent("bugStatisticsTab")
@ViewScope
public class StatisticsTab implements BugTab {
    @NonNull private final DataService dataService;

    @Autowired
    public StatisticsTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Panel root = new Panel(new Statistics(QReport.report.bug.id.eq(bug.getId()), dataService));
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        return root;
    }

    @Override
    public String getCaption() {
        return "Statistics";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
