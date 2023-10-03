/*
 * (C) Copyright 2019-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.NOT_NULL
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.comboBox
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.downloadButton
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.jooq.Condition
import org.jooq.Field
import org.springframework.security.core.context.SecurityContextHolder
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@View
class ExportAppAdminCard(
    reportRepository: ReportRepository,
    routeParams: RouteParams,
) : AdminCard() {
    private val appId = routeParams.appId()

    init {
        content {
            setHeader(Translatable.createLabel(Messages.EXPORT))
            val mailBox = comboBox(reportRepository.get(appId, REPORT.USER_EMAIL), Messages.BY_MAIL) {
                setWidthFull()
            }
            val idBox = comboBox(reportRepository.get(appId, REPORT.INSTALLATION_ID), Messages.BY_ID) {
                setWidthFull()
            }
            val authentication = SecurityContextHolder.getContext().authentication
            downloadButton(StreamResource("reports.json", InputStreamFactory {
                SecurityContextHolder.getContext().authentication = authentication
                try {
                    val where = listOfNotNull(REPORT.USER_EMAIL.eqIfNotBlank(mailBox.value), REPORT.INSTALLATION_ID.eqIfNotBlank(idBox.value)).reduceOrNull(Condition::and)
                    ByteArrayInputStream(
                        if (where == null) ByteArray(0) else reportRepository.get(appId, REPORT.CONTENT.NOT_NULL, where, sorted = false)
                            .joinToString(", ", "[", "]") { it.data() }.toByteArray(StandardCharsets.UTF_8)
                    )
                } finally {
                    SecurityContextHolder.getContext().authentication = null
                }
            }), Messages.DOWNLOAD) {
                setSizeFull()
            }
        }
    }

    private fun Field<String?>.eqIfNotBlank(value: String?): Condition? = value?.takeIf { it.isNotBlank() }?.let { eq(it) }
}