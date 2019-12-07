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
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.statistics.Statistics;
import com.faendir.acra.ui.view.bug.BugView;
import com.faendir.acra.util.LocalSettings;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 11.10.18
 */
@UIScope
@SpringComponent("bugStatisticsTab")
@Route(value = "statistics", layout = BugView.class)
public class StatisticsTab extends BugTab<Div> {
    private final LocalSettings localSettings;

    @Autowired
    public StatisticsTab(@NonNull DataService dataService, @NonNull LocalSettings localSettings) {
        super(dataService);
        this.localSettings = localSettings;
        getContent().setSizeFull();
    }

    @Override
    protected void init(Bug bug) {
        getContent().removeAll();
        getContent().add(new Statistics(bug.getApp(), QReport.report.stacktrace.bug.eq(bug), getDataService(), localSettings));
    }
}
