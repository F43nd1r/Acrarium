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
import com.faendir.acra.model.Report
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.base.HasRoute
import com.faendir.acra.ui.base.HasRoute.ParametrizedParent
import com.faendir.acra.ui.base.HasSecureParameter
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.CssGrid
import com.faendir.acra.ui.component.InstallationView
import com.faendir.acra.ui.component.Path
import com.faendir.acra.ui.component.Path.ParametrizedTextElement
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.honorWhitespaces
import com.faendir.acra.ui.ext.secondary
import com.faendir.acra.ui.view.MainView
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.Utils
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.lang.NonNull
import org.xbib.time.pretty.PrettyTime
import java.util.*
import kotlin.math.log10
import kotlin.math.max

/**
 * @author lukas
 * @since 17.09.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = MainView::class)
class ReportView(private val dataService: DataService, avatarService: AvatarService) : Composite<Div>(), HasSecureParameter<String>, HasRoute {
    private val version: Label
    private val date: Label
    private val installation: InstallationView
    private val email: Label
    private val comment: Label
    private val mappedStacktraceLabel: Translatable<Label>
    private val unmappedStacktraceLabel: Translatable<Label>
    private val stacktrace: Label
    private val attachments: Div
    private val details: Card
    private lateinit var report: Report
    private val prettyTime: PrettyTime = PrettyTime(Locale.US)

    init {
        val summaryLayout = CssGrid()
        summaryLayout.setTemplateColumns("auto auto")
        summaryLayout.setColumnGap(1, Unit.EM)
        summaryLayout.setJustifyItems(CssGrid.JustifyMode.START)
        summaryLayout.setAlignItems(CssGrid.AlignMode.FIRST_BASELINE)
        val installationLabel = Translatable.createLabel(Messages.USER).with { secondary() }
        version = Label()
        date = Label()
        installation = InstallationView(avatarService)
        email = Label()
        comment = Label()
        mappedStacktraceLabel = Translatable.createLabel(Messages.DE_OBFUSCATED_STACKTRACE).with { secondary() }
        unmappedStacktraceLabel = Translatable.createLabel(Messages.NO_MAPPING_STACKTRACE).with { secondary() }
        stacktrace = Label().honorWhitespaces()
        attachments = Div()
        summaryLayout.add(Translatable.createLabel(Messages.VERSION).with { secondary() }, version, Translatable.createLabel(Messages.DATE).with { secondary() }, date,
                installationLabel, installation, Translatable.createLabel(Messages.EMAIL).with { secondary() }, email,
                Translatable.createLabel(Messages.COMMENT).with { secondary() }, comment, mappedStacktraceLabel, unmappedStacktraceLabel, stacktrace,
                Translatable.createLabel(Messages.ATTACHMENTS).with { secondary() })
        summaryLayout.alignItems(CssGrid.AlignMode.CENTER, installationLabel)
        val summary = Card(summaryLayout)
        summary.setHeader(Translatable.createLabel(Messages.SUMMARY))
        details = Card()
        details.setHeader(Translatable.createLabel(Messages.DETAILS))
        content.add(summary, details)
    }

    override fun setParameterSecure(event: BeforeEvent, parameter: String) {
        val r = dataService.findReport(parameter)
        if (r.isPresent) {
            report = r.get()
            version.text = report.stacktrace.version.name
            date.text = prettyTime.formatUnrounded(report.date.toLocalDateTime())
            installation.setInstallationId(report.installationId)
            email.text = report.userEmail
            comment.text = report.userComment
            val mapping = report.stacktrace.version.mappings
            stacktrace.text = mapping?.let { Utils.retrace(report.stacktrace.stacktrace, it) } ?: report.stacktrace.stacktrace
            (if (mapping != null) unmappedStacktraceLabel else mappedStacktraceLabel).style["display"] = "none"
            attachments.removeAll()
            attachments.add(*dataService.findAttachments(report).map {
                Anchor(StreamResource(it.filename, InputStreamFactory { it.content.binaryStream }), it.filename).apply { element.setAttribute("download", true) }
            }.toTypedArray())
            details.removeAll()
            details.add(getLayoutForMap(report.jsonObject.toMap()))
        } else {
            event.rerouteToError(IllegalArgumentException::class.java)
        }
    }

    private fun getLayoutForMap(map: Map<String, *>): Div {
        val layout = CssGrid()
        layout.setTemplateColumns("auto auto")
        layout.setColumnGap(1, Unit.EM)
        layout.setJustifyItems(CssGrid.JustifyMode.START)
        map.entries.sortedBy { it.key }.forEach { layout.add(Label(it.key).secondary(), getComponentForContent(it.value)) }
        return layout
    }

    @Suppress("UNCHECKED_CAST")
    private fun getComponentForContent(value: Any?): Component {
        return when (value) {
            is Map<*, *> -> getLayoutForMap(value as Map<String, *>)
            is List<*> -> {
                val values = value as List<Any>
                val format = "%0${max(log10(values.size - 1.0), 0.0).toInt() + 1}d"
                getLayoutForMap(values.mapIndexed { i, v -> String.format(format, i) to v }.toMap())
            }
            else -> Label(value.toString()).honorWhitespaces()
        }
    }

    override fun getPathElement(): Path.Element<*> = ParametrizedTextElement(javaClass, report.id, Messages.REPORT_FROM, prettyTime.formatUnrounded(report.date.toLocalDateTime()))

    override fun getLogicalParent(): HasRoute.Parent<*> = ParametrizedParent(ReportTab::class.java, report.stacktrace.bug.id)
}