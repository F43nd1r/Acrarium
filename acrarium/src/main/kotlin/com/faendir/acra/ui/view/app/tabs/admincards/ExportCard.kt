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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.QReport
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.DownloadButton
import com.faendir.acra.ui.component.Translatable
import com.querydsl.core.types.dsl.BooleanExpression
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@UIScope
@SpringComponent
class ExportCard(dataService: DataService) : AdminCard(dataService) {
    init {
        setHeader(Translatable.createLabel(Messages.EXPORT))
    }

    override fun init(app: App) {
        removeContent()
        val mailBox = Translatable.createComboBox(dataService.getFromReports(app, null, QReport.report.userEmail), Messages.BY_MAIL)
        mailBox.setWidthFull()
        val idBox = Translatable.createComboBox(dataService.getFromReports(app, null, QReport.report.installationId), Messages.BY_ID)
        idBox.setWidthFull()
        val download = DownloadButton(StreamResource("reports.json", InputStreamFactory {
            var where: BooleanExpression? = null
            val mail = mailBox.content.value
            val id = idBox.content.value
            if (mail != null && mail.isNotEmpty()) {
                where = QReport.report.userEmail.eq(mail)
            }
            if (id != null && id.isNotEmpty()) {
                where = QReport.report.installationId.eq(id).and(where)
            }
            ByteArrayInputStream(if (where == null) ByteArray(0) else dataService.getFromReports(app, where, QReport.report.content, QReport.report.id)
                    .joinToString(", ", "[", "]").toByteArray(StandardCharsets.UTF_8))
        }), Messages.DOWNLOAD)
        download.setSizeFull()
        add(mailBox, idBox, download)
    }
}