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
package com.faendir.acra.ui.view.bug.tabs

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.Bug
import com.faendir.acra.model.Permission
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Align
import com.faendir.acra.ui.ext.honorWhitespaces
import com.faendir.acra.ui.ext.setAlignSelf
import com.faendir.acra.ui.view.bug.BugView
import com.faendir.acra.util.retrace
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.apache.catalina.security.SecurityUtil

/**
 * @author lukas
 * @since 19.11.18
 */
@UIScope
@SpringComponent
@Route(value = "stacktrace", layout = BugView::class)
class StacktraceTab(dataService: DataService) : BugTab<Div>(dataService), HasSize, HasStyle {

    init {
        setWidthFull()
        style["overflow"] = "auto"
    }

    override fun init(bug: Bug) {
        content.removeAll()
        val stacktraces = dataService.getStacktraces(bug)
        for (stacktrace in stacktraces) {
            val mapping = stacktrace.version.mappings
            val trace = mapping?.let { stacktrace.retrace(it) } ?: stacktrace.stacktrace
            val layout = FlexLayout(Label(trace).honorWhitespaces())
            layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN)
            if(stacktraces.size > 1 && SecurityUtils.hasPermission(bug.app, Permission.Level.EDIT)) {
                layout.add(Translatable.createButton(Messages.NOT_SAME_BUG) {
                    dataService.unmergeStacktrace(stacktrace)
                    init(bug)
                }.with {
                    icon = Icon(VaadinIcon.UNLINK)
                    removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    setAlignSelf(Align.END)
                })
            }
            val card = Card(layout)
            card.allowCollapse = true
            card.setHeader(Translatable.createLabel(Messages.STACKTRACE_TITLE, stacktrace.version.name, trace.lines().first()))
            content.children.findAny().ifPresent { card.collapse() }
            content.add(card)
        }
    }
}