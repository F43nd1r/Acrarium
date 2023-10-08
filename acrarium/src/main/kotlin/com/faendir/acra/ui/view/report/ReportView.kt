/*
 * (C) Copyright 2020-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.report

import com.faendir.acra.domain.AvatarService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.*
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.Translatable.Companion.createSpan
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.bug.tabs.ReportBugTab
import com.faendir.acra.ui.view.installation.InstallationView
import com.faendir.acra.ui.view.main.MainView
import com.faendir.acra.util.retrace
import com.faendir.acra.util.toUtcLocal
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.json.JSONObject
import org.xbib.time.pretty.PrettyTime
import java.util.*
import kotlin.math.log10
import kotlin.math.max

@View
@Route(value = "app/:$PARAM_APP/bug/:$PARAM_BUG/report/:$PARAM_REPORT", layout = MainView::class)
@LogicalParent(ReportBugTab::class)
@RequiresPermission(Permission.Level.VIEW)
@RequiresPermission(Permission.Level.VIEW)
class ReportView(
    private val reportRepository: ReportRepository,
    versionRepository: VersionRepository,
    avatarService: AvatarService,
    routeParams: RouteParams,
) : Composite<Div>(),
    HasAcrariumTitle {
    private val report = reportRepository.find(routeParams.reportId()) ?: throw NotFoundException()
    private val prettyTime: PrettyTime = PrettyTime(Locale.US)

    init {
        val version = versionRepository.find(report.appId, report.versionKey) ?: throw NotFoundException()
        content {
            card {
                setHeader(createSpan(Messages.SUMMARY))
                gridLayout {
                    setTemplateColumns("max-content max-content")
                    setColumnGap(1, SizeUnit.EM)
                    setJustifyItems(JustifyItems.START)
                    setAlignItems(Align.FIRST_BASELINE)

                    translatableSpan(Messages.VERSION) { secondary() }
                    gridLayout {
                        setTemplateColumns("max-content max-content")
                        setColumnGap(1, SizeUnit.EM)
                        translatableSpan(Messages.NAME) { secondary() }
                        span(version.name)
                        translatableSpan(Messages.VERSION_CODE) { secondary() }
                        span(version.code.toString())
                        translatableSpan(Messages.VERSION_FLAVOR) { secondary() }
                        span(version.flavor)
                    }
                    translatableSpan(Messages.DATE) { secondary() }
                    span(prettyTime.formatUnrounded(report.date.toUtcLocal()))
                    translatableSpan(Messages.INSTALLATION) {
                        secondary()
                        setAlignSelf(Align.CENTER)
                    }
                    routerLink(
                        com.faendir.acra.ui.view.installation.tabs.ReportInstallationTab::class.java,
                        RouteParameters(InstallationView.getNavigationParams(report.appId, report.installationId))
                    ) {
                        installationView(avatarService, report.installationId)
                    }
                    translatableSpan(Messages.EMAIL) { secondary() }
                    span(report.userEmail ?: "")
                    translatableSpan(Messages.COMMENT) { secondary() }
                    span(report.userComment ?: "")
                    val mapping = version.mappings
                    translatableSpan(if (mapping != null) Messages.DE_OBFUSCATED_STACKTRACE else Messages.NO_MAPPING_STACKTRACE) { secondary() }
                    span(mapping?.let { report.stacktrace.retrace(it) } ?: report.stacktrace) { honorWhitespaces() }
                    translatableSpan(Messages.ATTACHMENTS) { secondary() }
                    div {
                        forEach(reportRepository.findAttachmentNames(report.id)) {
                            anchor(StreamResource(it, InputStreamFactory { reportRepository.loadAttachment(report.id, it)!!.inputStream() }), it) {
                                element.setAttribute("download", true)
                            }
                        }
                    }
                }
            }
            card {
                setHeader(createSpan(Messages.DETAILS))
                layoutForMap(JSONObject(report.content.data()).toMap())
            }
        }
    }

    private fun HasComponents.layoutForMap(map: Map<String, *>) {
        gridLayout {
            setTemplateColumns("max-content max-content")
            setColumnGap(1, SizeUnit.EM)
            setJustifyItems(JustifyItems.START)

            forEach(map.entries.sortedBy { it.key }) {
                span(it.key) { secondary() }
                componentForContent(it.value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun HasComponents.componentForContent(value: Any?) {
        when (value) {
            is Map<*, *> -> layoutForMap(value as Map<String, *>)
            is List<*> -> {
                val values = value as List<Any>
                val format = "%0${max(log10(values.size - 1.0), 0.0).toInt() + 1}d"
                layoutForMap(values.mapIndexed { i, v -> String.format(format, i) to v }.toMap())
            }

            else -> span(value.toString()) { honorWhitespaces() }
        }
    }

    override val title = TranslatableText(Messages.REPORT_FROM, prettyTime.formatUnrounded(report.date.toUtcLocal()))

    companion object {
        fun getNavigationParams(appId: AppId, bugId: BugId, reportId: String) = mapOf(
            PARAM_APP to appId.toString(),
            PARAM_BUG to bugId.toString(),
            PARAM_REPORT to reportId,
        )
    }
}