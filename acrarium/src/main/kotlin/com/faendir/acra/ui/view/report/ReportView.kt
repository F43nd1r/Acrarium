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
package com.faendir.acra.ui.view.report

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.IReport
import com.faendir.acra.model.Report
import com.faendir.acra.navigation.ParseReportParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.tabs.HasRoute
import com.faendir.acra.ui.component.tabs.Path
import com.faendir.acra.ui.ext.Align
import com.faendir.acra.ui.ext.JustifyItems
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.anchor
import com.faendir.acra.ui.ext.card
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.div
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.gridLayout
import com.faendir.acra.ui.ext.honorWhitespaces
import com.faendir.acra.ui.ext.installationView
import com.faendir.acra.ui.ext.label
import com.faendir.acra.ui.ext.secondary
import com.faendir.acra.ui.ext.setAlignItems
import com.faendir.acra.ui.ext.setAlignSelf
import com.faendir.acra.ui.ext.setColumnGap
import com.faendir.acra.ui.ext.setJustifyItems
import com.faendir.acra.ui.ext.translatableLabel
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.ui.view.main.MainView
import com.faendir.acra.util.PARAM_APP
import com.faendir.acra.util.PARAM_BUG
import com.faendir.acra.util.PARAM_REPORT
import com.faendir.acra.util.retrace
import com.github.appreciated.css.grid.sizes.MaxContent
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.xbib.time.pretty.PrettyTime
import java.util.*
import kotlin.math.log10
import kotlin.math.max

/**
 * @author lukas
 * @since 17.09.18
 */
@View
@Route(value = "app/:${PARAM_APP}/bug/:${PARAM_BUG}/report/:${PARAM_REPORT}", layout = MainView::class)
class ReportView(private val dataService: DataService, avatarService: AvatarService, @ParseReportParameter private val report: Report) : Composite<Div>(),
    HasRoute {
    private val prettyTime: PrettyTime = PrettyTime(Locale.US)

    init {
        content {
            card {
                setHeader(Translatable.createLabel(Messages.SUMMARY))
                gridLayout {
                    setTemplateColumns(MaxContent(), MaxContent())
                    setColumnGap(1, SizeUnit.EM)
                    setJustifyItems(JustifyItems.START)
                    setAlignItems(Align.FIRST_BASELINE)

                    translatableLabel(Messages.VERSION) { secondary() }
                    label(report.stacktrace.version.name)
                    translatableLabel(Messages.DATE) { secondary() }
                    label(prettyTime.formatUnrounded(report.date.toLocalDateTime()))
                    translatableLabel(Messages.USER) {
                        secondary()
                        setAlignSelf(Align.CENTER)
                    }
                    installationView(avatarService, report.installationId)
                    translatableLabel(Messages.EMAIL) { secondary() }
                    label(report.userEmail)
                    translatableLabel(Messages.COMMENT) { secondary() }
                    label(report.userComment)
                    val mapping = report.stacktrace.version.mappings
                    translatableLabel(if (mapping != null) Messages.DE_OBFUSCATED_STACKTRACE else Messages.NO_MAPPING_STACKTRACE) { secondary() }
                    label(mapping?.let { report.stacktrace.retrace(it) } ?: report.stacktrace.stacktrace) { honorWhitespaces() }
                    translatableLabel(Messages.ATTACHMENTS) { secondary() }
                    div {
                        forEach(dataService.findAttachments(report)) {
                            anchor(StreamResource(it.filename, InputStreamFactory { it.content.binaryStream }), it.filename) {
                                element.setAttribute("download", true)
                            }
                        }
                    }
                }
            }
            card {
                setHeader(Translatable.createLabel(Messages.DETAILS))
                layoutForMap(report.jsonObject.toMap())
            }
        }
    }

    private fun HasComponents.layoutForMap(map: Map<String, *>) {
        gridLayout {
            setTemplateColumns(MaxContent(), MaxContent())
            setColumnGap(1, SizeUnit.EM)
            setJustifyItems(JustifyItems.START)

            forEach(map.entries.sortedBy { it.key }) {
                label(it.key) { secondary() }
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
            else -> label(value.toString()) { honorWhitespaces() }
        }
    }

    override val pathElement: Path.Element<*> =
        Path.Element(this::class, getNavigationParams(report), Messages.REPORT_FROM, prettyTime.formatUnrounded(report.date.toLocalDateTime()))

    override val logicalParent = ReportTab::class

    companion object {
        fun getNavigationParams(report: IReport) = mapOf(
            PARAM_APP to report.stacktrace.bug.app.id.toString(),
            PARAM_BUG to report.stacktrace.bug.id.toString(),
            PARAM_REPORT to report.id
        )
    }
}