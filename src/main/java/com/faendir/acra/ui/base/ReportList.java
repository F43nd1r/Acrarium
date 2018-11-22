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

package com.faendir.acra.ui.base;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.Report;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.view.report.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author lukas
 * @since 17.09.18
 */
public class ReportList extends MyGrid<Report>{
    public ReportList(@NonNull App app, @NonNull QueryDslDataProvider<Report> dataProvider, @NonNull AvatarService avatarService, @NonNull Consumer<Report> reportDeleter) {
        super(dataProvider);
        setSelectionMode(Grid.SelectionMode.NONE);
        addColumn(new ComponentRenderer<>(avatarService::getAvatar) , QReport.report.installationId, Messages.USER);
        addColumn(new TimeSpanRenderer<>(Report::getDate), QReport.report.date, Messages.DATE);
        addColumn(report -> report.getStacktrace().getVersion().getCode(), QReport.report.stacktrace.version.code, Messages.APP_VERSION);
        addColumn(Report::getAndroidVersion, QReport.report.androidVersion, Messages.ANDROID_VERSION);
        addColumn(Report::getPhoneModel, QReport.report.phoneModel, Messages.DEVICE);
        addColumn(report -> report.getStacktrace().getStacktrace().split("\n", 2)[0], QReport.report.stacktrace.stacktrace, Messages.STACKTRACE).setFlexGrow(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(new ComponentRenderer<>(report -> new Button(new Icon(VaadinIcon.TRASH),
                    (ComponentEventListener<ClickEvent<Button>>) event -> new Popup().setTitle(Messages.DELETE_REPORT_CONFIRM).addYesNoButtons(p -> {
                        reportDeleter.accept(report);
                        getDataProvider().refreshAll();
                    }, true).show())));
        }
        addOnClickNavigation(ReportView.class, Report::getId);
    }
}
