/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.view.app.tabs.admincards;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.DownloadButton;
import com.faendir.acra.ui.component.Translatable;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static com.faendir.acra.model.QReport.report;

@UIScope
@SpringComponent
public class ExportCard extends AdminCard {
    public ExportCard(DataService dataService) {
        super(dataService);
        setHeader(Translatable.createLabel(Messages.EXPORT));

    }

    @Override
    public void init(App app) {
        removeContent();
        Translatable<ComboBox<String>> mailBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.userEmail), Messages.BY_MAIL);
        mailBox.setWidthFull();
        Translatable<ComboBox<String>> idBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.installationId), Messages.BY_ID);
        idBox.setWidthFull();
        DownloadButton download = new DownloadButton(new StreamResource("reports.json", () -> {
            BooleanExpression where = null;
            String mail = mailBox.getContent().getValue();
            String id = idBox.getContent().getValue();
            if (mail != null && !mail.isEmpty()) {
                where = report.userEmail.eq(mail);
            }
            if (id != null && !id.isEmpty()) {
                where = report.installationId.eq(id).and(where);
            }
            if (where == null) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(getDataService().getFromReports(app, where, report.content, report.id).stream().collect(Collectors.joining(", ", "[", "]")).getBytes(StandardCharsets.UTF_8));
        }), Messages.DOWNLOAD);
        download.setSizeFull();
        add(mailBox, idBox, download);
    }
}
