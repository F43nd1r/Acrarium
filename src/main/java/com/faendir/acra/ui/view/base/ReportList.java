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
package com.faendir.acra.ui.view.base;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.Report;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.report.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> {
    public static final String CAPTION = "Reports";

    public ReportList(App app, @NonNull NavigationManager navigationManager, @NonNull Consumer<Report> reportDeleter, @NonNull QueryDslDataProvider<Report> reportProvider) {
        super(CAPTION, reportProvider);
        setId(CAPTION);
        setSelectionMode(Grid.SelectionMode.NONE);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), QReport.report.date, "Date"), SortDirection.DESCENDING);
        addColumn(report -> report.getStacktrace().getVersionCode(), QReport.report.stacktrace.versionCode, "App Version");
        addColumn(Report::getAndroidVersion, QReport.report.androidVersion, "Android Version");
        addColumn(Report::getPhoneModel, QReport.report.phoneModel, "Device");
        addColumn(report -> report.getStacktrace().getStacktrace().split("\n", 2)[0], QReport.report.stacktrace.stacktrace, "Stacktrace").setExpandRatio(1)
                .setMinimumWidthFromContent(false);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(report -> "Delete",
                    new ButtonRenderer<>(e -> new Popup().setTitle("Confirm")
                            .addComponent(new Label("Are you sure you want to delete this report?"))
                            .addYesNoButtons(p -> reportDeleter.accept(e.getItem()), true)
                            .show())).setSortable(false);
        }
        addOnClickNavigation(navigationManager, ReportView.class, e -> e.getItem().getId());
    }
}
