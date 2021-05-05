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
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.comboBox
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.downloadButton
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.StringPath
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.springframework.security.core.context.SecurityContextHolder
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@View
class ExportCard(dataService: DataService, @ParseAppParameter app: App) : AdminCard(dataService) {
    init {
        content {
            setHeader(Translatable.createLabel(Messages.EXPORT))
            val mailBox = comboBox(dataService.getFromReports(app, QReport.report.userEmail), Messages.BY_MAIL) {
                setWidthFull()
            }
            val idBox = comboBox(dataService.getFromReports(app, QReport.report.installationId), Messages.BY_ID) {
                setWidthFull()
            }
            val authentication = SecurityContextHolder.getContext().authentication
            downloadButton(StreamResource("reports.json", InputStreamFactory {
                SecurityContextHolder.getContext().authentication = authentication
                try {
                    val where = null.eqIfNotBlank(QReport.report.userEmail, mailBox.value).eqIfNotBlank(QReport.report.installationId, idBox.value)
                    ByteArrayInputStream(
                        if (where == null) ByteArray(0) else dataService.getFromReports(app, QReport.report.content, where, sorted = false)
                            .joinToString(", ", "[", "]").toByteArray(StandardCharsets.UTF_8)
                    )
                } finally {
                    SecurityContextHolder.getContext().authentication = null
                }
            }), Messages.DOWNLOAD) {
                setSizeFull()
            }
        }
    }

    private fun BooleanExpression?.eqIfNotBlank(path: StringPath, value: String?): BooleanExpression? {
        return value?.takeIf { it.isNotBlank() }?.let { path.eq(it).and(this) } ?: this
    }
}