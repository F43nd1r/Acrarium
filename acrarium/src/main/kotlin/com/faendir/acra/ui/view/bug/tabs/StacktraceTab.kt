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
import com.faendir.acra.navigation.ParseBugParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Align
import com.faendir.acra.ui.ext.card
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.honorWhitespaces
import com.faendir.acra.ui.ext.label
import com.faendir.acra.ui.ext.setAlignSelf
import com.faendir.acra.ui.ext.translatableButton
import com.faendir.acra.ui.view.bug.BugView
import com.faendir.acra.util.retrace
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 19.11.18
 */
@View
@Route(value = "stacktrace", layout = BugView::class)
class StacktraceTab(private val dataService: DataService, @ParseBugParameter private val bug: Bug) : BugTab<Div>(bug) {

    init {
        content {
            setWidthFull()
            style["overflow"] = "auto"
            val stacktraces = dataService.getStacktraces(bug).toMutableList()
            forEach(stacktraces) { stacktrace ->
                card {
                    val mapping = stacktrace.version.mappings
                    val trace = mapping?.let { stacktrace.retrace(it) } ?: stacktrace.stacktrace
                    allowCollapse = true
                    setHeader(Translatable.createLabel(Messages.STACKTRACE_TITLE, stacktrace.version.name, trace.lines().first()))
                    this@content.children.findAny().ifPresent { isCollapsed = true }
                    flexLayout {
                        setFlexDirection(FlexLayout.FlexDirection.COLUMN)
                        label(trace) { honorWhitespaces() }
                        if (stacktraces.size > 1 && SecurityUtils.hasPermission(bug.app, Permission.Level.EDIT)) {
                            translatableButton(Messages.NOT_SAME_BUG) {
                                dataService.unmergeStacktrace(stacktrace)
                                this@content.remove(this@card)
                                stacktraces.remove(stacktrace)
                                if (stacktraces.size == 1) {
                                    this@content.children.forEach { card ->
                                        card.children.filter { it is Button }.forEach { (card as Card).remove(it) }
                                    }
                                }
                            }.with {
                                icon = Icon(VaadinIcon.UNLINK)
                                removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                                setAlignSelf(Align.END)
                            }
                        }
                    }
                }
            }
        }
    }
}