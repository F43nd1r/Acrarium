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
import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.Report;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.report.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ImageRenderer;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

import java.util.function.Consumer;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class ReportList extends MyGrid<Report> {

    public ReportList(@NonNull App app, @NonNull NavigationManager navigationManager, @NonNull AvatarService avatarService, @NonNull Consumer<Report> reportDeleter, @NonNull QueryDslDataProvider<Report> reportProvider, I18N i18n) {
        super(reportProvider, i18n, Messages.REPORTS);
        setSelectionMode(Grid.SelectionMode.NONE);
        addColumn(avatarService::getAvatar, new ImageRenderer<>(), QReport.report.installationId, Messages.USER);
        sort(addColumn(Report::getDate, new TimeSpanRenderer(), QReport.report.date, Messages.DATE), SortDirection.DESCENDING);
        addColumn(report -> report.getStacktrace().getVersion().getCode(), QReport.report.stacktrace.version.code, Messages.APP_VERSION);
        addColumn(Report::getAndroidVersion, QReport.report.androidVersion, Messages.ANDROID_VERSION);
        addColumn(Report::getPhoneModel, QReport.report.phoneModel, Messages.DEVICE);
        addColumn(report -> report.getStacktrace().getStacktrace().split("\n", 2)[0], QReport.report.stacktrace.stacktrace, Messages.STACKTRACE).setExpandRatio(1)
                .setMinimumWidthFromContent(false);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            ButtonRenderer<Report> renderer = new ButtonRenderer<>(e -> new Popup(i18n, Messages.CONFIRM)
                    .addComponent(new I18nLabel(i18n, Messages.DELETE_REPORT_CONFIRM))
                    .addYesNoButtons(p -> {
                        reportDeleter.accept(e.getItem());
                        getDataProvider().refreshAll();
                    }, true)
                    .show());
            renderer.setHtmlContentAllowed(true);
            addColumn(report -> VaadinIcons.TRASH.getHtml(),
                    renderer).setSortable(false);
        }
        addOnClickNavigation(navigationManager, ReportView.class, e -> e.getItem().getId());
    }
}
