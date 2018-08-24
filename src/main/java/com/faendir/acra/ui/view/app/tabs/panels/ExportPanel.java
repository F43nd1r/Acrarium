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

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nComboBox;
import com.faendir.acra.i18n.Messages;
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
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class ExportPanel implements AdminPanel {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public ExportPanel(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }
    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        ComboBox<String> mailBox = new I18nComboBox<>(dataService.getFromReports(app, null, QReport.report.userEmail), i18n, Messages.BY_MAIL);
        mailBox.setEmptySelectionAllowed(true);
        mailBox.setSizeFull();
        ComboBox<String> idBox = new I18nComboBox<>(dataService.getFromReports(app, null, QReport.report.installationId), i18n, Messages.BY_ID);
        idBox.setEmptySelectionAllowed(true);
        idBox.setSizeFull();
        Button download = new I18nButton(e -> {
            if (idBox.getValue() == null && mailBox.getValue() == null) {
                Notification.show(i18n.get(Messages.NOTHING_SELECTED), Notification.Type.WARNING_MESSAGE);
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
        }, i18n, Messages.DOWNLOAD);
        download.setSizeFull();
        return new VerticalLayout(mailBox, idBox, download);
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.EXPORT);
    }

    @Override
    public String getId() {
        return "export";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
