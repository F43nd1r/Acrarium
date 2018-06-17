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

package com.faendir.acra.ui.view.app.tabs.panels;

import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.rest.RestReportInterface;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class ExportPanel implements AdminPanel {
    @NonNull private final DataService dataService;

    @Autowired
    public ExportPanel(@NonNull DataService dataService) {
        this.dataService = dataService;
    }
    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        ComboBox<String> mailBox = new ComboBox<>("By Email Address", dataService.getFromReports(QReport.report.stacktrace.bug.app.eq(app), QReport.report.userEmail));
        mailBox.setEmptySelectionAllowed(true);
        mailBox.setSizeFull();
        ComboBox<String> idBox = new ComboBox<>("By Installation ID", dataService.getFromReports(QReport.report.stacktrace.bug.app.eq(app), QReport.report.installationId));
        idBox.setEmptySelectionAllowed(true);
        idBox.setSizeFull();
        Button download = new Button("Download", e -> {
            if (idBox.getValue() == null && mailBox.getValue() == null) {
                Notification.show("Nothing selected", Notification.Type.WARNING_MESSAGE);
            } else {
                Page page = UI.getCurrent().getPage();
                page.open(UriComponentsBuilder.fromUri(page.getLocation())
                        .fragment(null)
                        .pathSegment(RestReportInterface.EXPORT_PATH)
                        .queryParam(RestReportInterface.PARAM_APP, app.getId())
                        .queryParam(RestReportInterface.PARAM_ID, idBox.getValue())
                        .queryParam(RestReportInterface.PARAM_MAIL, mailBox.getValue())
                        .build()
                        .toUriString(), null);
            }
        });
        download.setSizeFull();
        return new VerticalLayout(mailBox, idBox, download);
    }

    @Override
    public String getCaption() {
        return "Export";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
